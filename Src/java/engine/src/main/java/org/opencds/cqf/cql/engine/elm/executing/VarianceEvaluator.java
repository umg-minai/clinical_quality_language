package org.opencds.cqf.cql.engine.elm.executing;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.opencds.cqf.cql.engine.exception.InvalidOperatorArgument;
import org.opencds.cqf.cql.engine.execution.State;
import org.opencds.cqf.cql.engine.runtime.Quantity;

/*
Variance(argument List<Decimal>) Decimal
Variance(argument List<Quantity>) Quantity

The Variance operator returns the statistical variance of the elements in source.
If the source contains no non-null elements, null is returned.
If the source is null, the result is null.
Return types: BigDecimal & Quantity
*/

public class VarianceEvaluator {

    public static Object variance(Object source, State state) {

        if (source == null) {
            return null;
        }

        if (source instanceof Iterable<?> iterable) {

            if (!iterable.iterator().hasNext()) {
                return null;
            }

            Object mean = AvgEvaluator.avg(source, state);

            List<Object> newVals = new ArrayList<>();
            for (Object element : iterable) {
                if (element != null) {
                    if (element instanceof BigDecimal || element instanceof Quantity) {
                        final var diff = SubtractEvaluator.subtract(element, mean, state);
                        newVals.add(MultiplyEvaluator.multiply(diff, diff, state));
                    } else {
                        throw new InvalidOperatorArgument(
                                "Variance(List<Decimal>) or Variance(List<Quantity>)",
                                String.format(
                                        "Variance(List<%s>)", element.getClass().getName()));
                    }
                }
            }

            return DivideEvaluator.divide(
                    SumEvaluator.sum(newVals, state),
                    new BigDecimal(newVals.size() - 1),
                    state); // slight variation to Avg
        }

        throw new InvalidOperatorArgument(
                "Variance(List<Decimal>) or Variance(List<Quantity>)",
                String.format("Variance(%s)", source.getClass().getName()));
    }
}
