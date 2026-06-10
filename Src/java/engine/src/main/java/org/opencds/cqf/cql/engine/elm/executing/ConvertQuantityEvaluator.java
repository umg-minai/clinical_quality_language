package org.opencds.cqf.cql.engine.elm.executing;

import org.cqframework.cql.cql2elm.UnitConverter;
import org.opencds.cqf.cql.engine.exception.InvalidOperatorArgument;
import org.opencds.cqf.cql.engine.runtime.Quantity;

/*
    convert <quantity> to <unit>
    ConvertQuantity(argument Quantity, unit String)

    The ConvertQuantity operator converts a Quantity to an equivalent Quantity with the given unit. If the unit of the
        input quantity can be converted to the target unit, the result is an equivalent Quantity with the target unit.
        Otherwise, the result is null.

    Note that implementations are not required to support quantity conversion. Implementations that do support unit
        conversion shall do so according to the conversion specified by UCUM. Implementations that do not support unit
        conversion shall throw an error if an unsupported unit conversion is requested with this operation.

    If either argument is null, the result is null.

    define ConvertQuantity: ConvertQuantity(5 'mg', 'g')
    define ConvertSyntax: convert 5 'mg' to 'g'

*/

public class ConvertQuantityEvaluator {

    public static Object convertQuantity(final Object argument, final Object unit, final UnitConverter unitConverter) {
        if (argument == null || unit == null) {
            return null;
        }
        if (!(argument instanceof Quantity quantity) || !(unit instanceof String toUnit)) {
            throw new InvalidOperatorArgument(
                    "ConvertQuantity(Quantity, String)",
                    String.format(
                            "ConvertQuantity(%s, %s)",
                            argument.getClass().getName(), unit.getClass().getName()));
        }
        final var fromUnit = quantity.getUnit();
        if (fromUnit.equals(toUnit)) {
            return quantity;
        }
        if (unitConverter == null) {
            return null;
        }

        final var convertedValue = unitConverter.convert(quantity.getValue(), fromUnit, toUnit);
        return convertedValue != null ? new Quantity().withValue(convertedValue).withUnit(toUnit) : null;
    }
}
