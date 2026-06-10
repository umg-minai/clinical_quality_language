package org.opencds.cqf.cql.engine.elm.executing;

import java.util.*;
import org.opencds.cqf.cql.engine.execution.CqlEngine;
import org.opencds.cqf.cql.engine.execution.State;

/**
 * {@code distinct(argument List<T>) List<T>}
 * <br/>
 * The distinct operator returns the given list with duplicates eliminated using equality semantics.
 * <br/>
 * If the argument is null, the result is null.
 */
public class DistinctEvaluator {

    public static List<Object> distinct(final Iterable<?> source, final State state) {
        if (source == null) {
            return null;
        }
        final var hedisMode = state.getEngineOptions().contains(CqlEngine.Options.EnableHedisCompatibilityMode);
        final var seen = new IdentityHashMap<Object, Boolean>();
        final var distinctViaEquals = new HashSet<>();
        final var result = new ArrayList<>();
        for (Object element : source) {
            // Check whether element itself is already in the result.  This works correctly if element is null.
            if (seen.containsKey(element)) {
                continue;
            }
            // If element itself is not in the result, we can mark it as "seen" since either it will be added now or
            // there is an element in the result that is equal to element. In either case, if we see element itself
            // again, we can skip it.
            seen.put(element, Boolean.TRUE);
            // TODO: explain
            //
            // Hedis compatibility mode make the InEvaluator use equivalent instead of equal so don't use Object#equals
            // when in Hedis compatibility mode.
            if (element == null || (!hedisMode && state.canUseEquals(element))) {
                if (!distinctViaEquals.contains(element)) {
                    distinctViaEquals.add(element);
                    result.add(element);
                }
            } else {
                // Look for an element in result that is equal to element and add element if there is none.
                final var in = InEvaluator.in(element, result, null, state);
                if (in != null && !in) {
                    result.add(element);
                }
            }
        }
        return result;
    }
}
