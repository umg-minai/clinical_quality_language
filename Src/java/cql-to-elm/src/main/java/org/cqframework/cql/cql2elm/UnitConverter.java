package org.cqframework.cql.cql2elm;

import org.fhir.ucum.Decimal;
import org.fhir.ucum.UcumException;
import org.fhir.ucum.UcumService;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * This class converts quantity values between different physical units using the {@link UcumService}.
 * <br/>
 * The difference compared to using {@link UcumService} directly is that this class caches conversion factors and
 * impossibility of conversion when possible which speeds up conversions by several orders of magnitude.
 * <br/>
 * To decide whether a conversion factor can be computed cached, this class converts then numbers 1 and 2 using
 * {@link UcumService} and checks whether the ratio between the results is also 2.
 */
public class UnitConverter {

    // Key for looking up conversions.
    private record UnitPair(String fromUnit, String toUnit) {}

    // The three possible conversion outcomes for caching:
    // 1. Cached factor which can be used to perform conversions without involving the UCUMService
    // 2. Marker that the conversion must be performed using the UCUMService
    // 3. Marker that the conversion cannot be performed and null should be returned
    private sealed interface Result permits Factor, ComplicatedConversion, NoConversion {}

    private record Factor(BigDecimal value) implements Result {}

    private record ComplicatedConversion() implements Result {}

    private record NoConversion() implements Result {}

    // Cache of previously seen conversions.
    private final Map<UnitPair, Result> conversionFactors = new HashMap<>();

    // UCUM service for obtaining conversion factors and performing "complicated" conversions that cannot be handled
    // with a cached conversion factor.
    private final UcumService ucumService;

    public UnitConverter(final UcumService ucumService) {
        this.ucumService = ucumService;
    }

    private boolean bigDecimalsEqualOrClose(final BigDecimal bigDecimal1, final BigDecimal bigDecimal2) {
        return bigDecimal1.equals(bigDecimal2)
                || (bigDecimal1.subtract(bigDecimal2).abs().compareTo(new BigDecimal("1e-10")) < 0);
    }

    private boolean decimalsEqualOrClose(final Decimal decimal1, final Decimal decimal2) {
        return decimal1.equals(decimal2)
                // Do an approximate comparison if the values are not exactly equal.  Unfortunately the UCUM
                // Decimal class does not handle subtraction very well so we use BigDecimal instead.
                || bigDecimalsEqualOrClose(new BigDecimal(decimal1.asDecimal()), new BigDecimal(decimal2.asDecimal()));
    }

    private Result getConversionFactor(final String fromUnit, final String toUnit) {
        final var key = new UnitPair(fromUnit, toUnit);
        synchronized (this) {
            return this.conversionFactors.computeIfAbsent(key, key_ -> {
                // Try to convert the numbers 1 and 2 from fromUnit to toUnit and analyze the results.  If the
                // conversion attempt throws a UcumException, record the conversion as impossible.  If the ratio between
                // the results for 1 and 2 is also 2, assume the conversion is linear and record the conversion factor.
                // If the ratio is not 2, assume the conversion is non-linear and record the conversion as "complicated"
                // which means the UcumService has to be used to perform the conversion.
                Decimal factor;
                try {
                    factor = this.ucumService.convert(new Decimal("1"), fromUnit, toUnit);
                    final var factor2 = this.ucumService.convert(new Decimal("2"), fromUnit, toUnit);
                    final var factorTimesTwo = factor.multiply(new Decimal("2"));
                    if (!decimalsEqualOrClose(factorTimesTwo, factor2)) {
                        return new ComplicatedConversion();
                    }
                } catch (UcumException ignored) {
                    return new NoConversion();
                }
                return new Factor(new BigDecimal(factor.asDecimal()));
            });
        }
    }

    private BigDecimal convertComplicated(final BigDecimal value, final String fromUnit, final String toUnit) {
        try {
            // Ensure that the precision of the constructed Decimal is high enough to produce the same rounding behavior
            // as the calculation which uses a cached conversion factor (the latter uses only BigDecimal values and can
            // therefore behave differently depending on the chosen precision).
            final var decimalValue = new Decimal(value.toPlainString(), value.precision() + 2);
            final var decimalResult = this.ucumService.convert(decimalValue, fromUnit, toUnit);
            return new BigDecimal(decimalResult.asDecimal());
        } catch (Exception e) {
            return null;
        }
    }

    public BigDecimal convert(final BigDecimal value, final String fromUnit, final String toUnit) {
        final var result = getConversionFactor(fromUnit, toUnit);
        if (result instanceof Factor factor) {
            return value.multiply(factor.value);
        } else if (result instanceof ComplicatedConversion) {
            return convertComplicated(value, fromUnit, toUnit);
        } else {
            return null;
        }
    }

}
