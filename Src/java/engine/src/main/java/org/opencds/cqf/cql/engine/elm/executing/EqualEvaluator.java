package org.opencds.cqf.cql.engine.elm.executing;

import java.math.BigDecimal;
import org.opencds.cqf.cql.engine.exception.InvalidOperatorArgument;
import org.opencds.cqf.cql.engine.execution.State;
import org.opencds.cqf.cql.engine.runtime.CqlList;
import org.opencds.cqf.cql.engine.runtime.CqlType;
import org.opencds.cqf.cql.engine.runtime.Interval;
import org.opencds.cqf.cql.engine.runtime.Quantity;

/*
*** NOTES FOR CLINICAL OPERATORS ***
=(left Code, right Code) Boolean
=(left Concept, right Concept) Boolean

The equal (=) operator for Codes and Concepts uses tuple equality semantics.
  This means that the operator will return true if and only if the values for each element by name are equal.
If either argument is null, or contains any null components, the result is null.

*** NOTES FOR INTERVAL ***
=(left Interval<T>, right Interval<T>) Boolean

The equal (=) operator for intervals returns true if and only if the intervals are over the same point type,
  and they have the same value for the starting and ending points of the intervals as determined by the Start and End operators.
If either argument is null, the result is null.

*** NOTES FOR LIST ***
=(left List<T>, right List<T>) Boolean

The equal (=) operator for lists returns true if and only if the lists have the same element type,
  and have the same elements by value, in the same order.
If either argument is null, or contains null elements, the result is null.

*/

public class EqualEvaluator {

    public static Boolean equal(final Object left, final Object right, final State state) {
        if (left == null || right == null) {
            return null;
        } else if (left instanceof Iterable<?> leftIterable && right instanceof Iterable<?> rightIterable) {
            return CqlList.equal(leftIterable, rightIterable, state);
        } else if (left instanceof Interval leftInterval && right instanceof Integer rightInteger) {
            return leftInterval.equal(rightInteger);
        } else if (right instanceof Interval rightInterval && left instanceof Integer leftInteger) {
            return rightInterval.equal(leftInteger);
        } else if (left instanceof BigDecimal leftBigDecimal && right instanceof BigDecimal rightBigDecimal) {
            return leftBigDecimal.compareTo(rightBigDecimal) == 0;
        } else if (left instanceof Quantity leftQuantity && right instanceof Quantity rightQuantity) {
            // Try the Quantity.equal method which implements "simple" rules such as the equality of alternate
            // spellings for "week" or "month".
            final var simpleResult = leftQuantity.equal(rightQuantity);
            if (simpleResult != null) {
                return simpleResult; // true or false
            } else {
                // The simple method indicated that the units are not comparable, try to convert the value of
                // rightQuantity to the unit of leftQuantity and check for equality again if the conversion is
                // possible.
                return UnitConversionHelper.computeWithConvertedUnits(
                        leftQuantity,
                        rightQuantity,
                        (commonUnit, leftValue, rightValue) -> EqualEvaluator.equal(leftValue, rightValue),
                        state);
            }
        } else if (left instanceof CqlType leftCqlType && right instanceof CqlType) {
            return leftCqlType.equal(right);
        } else if (left instanceof Boolean
                || left instanceof Integer
                || left instanceof Long
                || left instanceof String
                || (state != null && state.canUseEquals(left))) {
            return left.equals(right);
        } else if (!(right.getClass().isAssignableFrom(left.getClass())
                || left.getClass().isAssignableFrom(right.getClass()))) {
            return false;
        } else if (state != null) {
            return state.getEnvironment().objectEqual(left, right);
        } else {
            throw new InvalidOperatorArgument(String.format(
                    "Equal(%s, %s) requires Context and state was null",
                    left.getClass().getName(), right.getClass().getName()));
        }
    }

    public static Boolean equal(final Object left, final Object right) {
        return equal(left, right, null);
    }
}
