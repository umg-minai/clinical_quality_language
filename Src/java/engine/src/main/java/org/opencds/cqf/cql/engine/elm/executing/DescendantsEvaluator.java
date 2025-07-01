package org.opencds.cqf.cql.engine.elm.executing;

import java.util.ArrayList;
import org.opencds.cqf.cql.engine.runtime.Interval;
import org.opencds.cqf.cql.engine.runtime.Tuple;

public class DescendantsEvaluator {

    public static Object descendants(Object source) {
        if (source == null) {
            return null;
        }

        final var result = new ArrayList<>();
        collectDescendants(source, result);
        return result;
    }

    public static void collectDescendants(final Object source, final ArrayList<Object> into) {
        if (source instanceof Iterable) {
            for (Object element : (Iterable<?>) source) {
                collectDescendants(element, into);
            }
        } else if (source instanceof Tuple) {
            for (Object element : ((Tuple) source).getElements().values()) {
                collectDescendants(element, into);
            }
        } else if (source instanceof Interval interval) {
            collectDescendants(interval.getStart(), into);
            collectDescendants(interval.getEnd(), into);
        } else {
            into.add(source);
        }
    }
}
