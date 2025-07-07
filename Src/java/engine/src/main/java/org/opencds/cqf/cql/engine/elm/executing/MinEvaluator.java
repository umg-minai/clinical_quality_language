package org.opencds.cqf.cql.engine.elm.executing;

import org.opencds.cqf.cql.engine.exception.InvalidOperatorArgument;
import org.opencds.cqf.cql.engine.execution.State;

/*
Min(argument List<Integer>) Integer
Min(argument List<Long>) Long
Min(argument List<Decimal>) Decimal
Min(argument List<Quantity>) Quantity
Min(argument List<Date>) Date
Min(argument List<DateTime>) DateTime
Min(argument List<Time>) Time
Min(argument List<String>) String

The Min operator returns the minimum element in the source. Comparison semantics are defined by the
    Comparison Operators for the type of value being aggregated.

If the source contains no non-null elements, null is returned.

If the source is null, the result is null.
*/

public class MinEvaluator {

    public static Object min(Object source, State state) {
        if (source == null) {
            return null;
        }

        if (source instanceof Iterable<?> iterable) {
            Object min = null;
            for (var element : iterable) {
                if (element == null) {
                    continue;
                }
                if (min == null) {
                    min = element;
                    continue;
                }
                final var isLess = LessEvaluator.less(element, min, state);
                if (isLess != null && isLess) {
                    min = element;
                }
            }
            return min;
        }

        throw new InvalidOperatorArgument(
                "Min(List<Integer>), Min(List<Long>), Min(List<Decimal>), Min(List<Quantity>), Min(List<Date>), Min(List<DateTime>), Min(List<Time>) or Min(List<String>)",
                String.format("Min(%s)", source.getClass().getName()));
    }
}
