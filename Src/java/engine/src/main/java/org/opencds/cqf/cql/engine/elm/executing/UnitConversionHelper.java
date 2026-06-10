package org.opencds.cqf.cql.engine.elm.executing;

import java.math.BigDecimal;
import org.cqframework.cql.cql2elm.UnitConverter;
import org.opencds.cqf.cql.engine.execution.State;
import org.opencds.cqf.cql.engine.runtime.Quantity;

public class UnitConversionHelper {

    // java.util.*Function only goes up to two parameters so we have to define an ad-hoc interface here.
    public interface Computation<R> {
        R compute(final String commonUnit, final BigDecimal leftValue, final BigDecimal rightValue);
    }

    public static <R> R computeWithConvertedUnits(
            final Quantity left, final Quantity right, final Computation<R> computation, final State state) {
        final var leftUnit = left.getUnit();
        final var rightUnit = right.getUnit();
        // If the units are equal, perform the computation without any conversion.
        if (leftUnit.equals(rightUnit)) {
            return computation.compute(leftUnit, left.getValue(), right.getValue());
        } else {
            // If the units are not equal, try to convert between the different units. Try the conversion in both
            // directions and select the one for which the result of the computation will be expressed in the more
            // granular unit.
            final var leftValue = left.getValue();
            final var rightValue = right.getValue();
            final var unitConverter = state.getEnvironment().getLibraryManager().getUnitConverter();
            final var rightConverted = convertIfLessGranular(unitConverter, rightValue, rightUnit, leftUnit);
            if (rightConverted != null) {
                return computation.compute(leftUnit, leftValue, rightConverted);
            } else {
                final var leftConverted = convertIfLessGranular(unitConverter, leftValue, leftUnit, rightUnit);
                if (leftConverted != null) {
                    return computation.compute(rightUnit, leftConverted, rightValue);
                }
            }
        }
        // If the units were neither equal not convertible, don't perform the computation and return null.
        return null;
    }

    public static Integer compareQuantities(
            final Quantity leftQuantity, final Quantity rightQuantity, final State state) {
        if (leftQuantity.getValue() == null || rightQuantity.getValue() == null) {
            return null;
        } else {
            var nullableCompareTo = leftQuantity.nullableCompareTo(rightQuantity);
            if (nullableCompareTo == null) {
                nullableCompareTo = computeWithConvertedUnits(
                        leftQuantity,
                        rightQuantity,
                        (commonUnit, leftValue, rightValue) -> leftValue.compareTo(rightValue),
                        state);
            }
            return nullableCompareTo;
        }
    }

    private static BigDecimal convertIfLessGranular(
            final UnitConverter unitConverter, final BigDecimal value, final String fromUnit, final String toUnit) {
        final var converted = unitConverter.convert(value, fromUnit, toUnit);
        // If the units are equal but spelled differently (for example 'g/m' vs 'g.m-1'), the numeric value may be
        // the same as before, so accept convertedDecimal and decimal being equal as "less granular".
        if (converted != null && converted.compareTo(value) >= 0) {
            return converted;
        } else {
            return null;
        }
    }
}
