package org.opencds.cqf.cql.engine.elm.executing;

import java.util.ArrayList;
import org.opencds.cqf.cql.engine.exception.InvalidOperatorArgument;
import org.opencds.cqf.cql.engine.execution.State;

/*
PopulationVariance(argument List<Decimal>) Decimal
PopulationVariance(argument List<Quantity>) Quantity

The PopulationVariance operator returns the statistical population variance of the elements in source.
If the source contains no non-null elements, null is returned.
If the source is null, the result is null.
Return types: BigDecimal & Quantity
*/

public class PopulationVarianceEvaluator {

    public static Object popVariance(Object source, State state) {

        if (source == null) {
            return null;
        }

        if (source instanceof Iterable<?> iterable) {
            if (!iterable.iterator().hasNext()) {
                return null;
            }
            final var mean = AvgEvaluator.avg(source, state);

            final var newVals = new ArrayList<>();
            iterable.forEach(element -> {
                final var diff = SubtractEvaluator.subtract(element, mean, state);
                newVals.add(MultiplyEvaluator.multiply(diff, diff, state));
            });

            return AvgEvaluator.avg(newVals, state);
        }

        throw new InvalidOperatorArgument(
                "PopulationVariance(List<Decimal>) or PopulationVariance(List<Quantity>)",
                String.format("PopulationVariance(%s)", source.getClass().getName()));
    }
}
