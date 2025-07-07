package org.opencds.cqf.cql.engine.elm.executing;

import java.util.ArrayList;
import java.util.List;
import org.opencds.cqf.cql.engine.exception.InvalidOperatorArgument;

/*
 * The ELM Slice operation is the foundation for 3 CQL operators:
 *
 * Skip(argument List<T>, number Integer) List<T>
 * X.skip(Y) -> Slice(X,Y,null)
 * The Skip operator returns the elements in the list, skipping the first number of elements.
 * If the source list has fewer elements than should be skipped, the result is empty.
 * If the source list is null, the result is null.
 * If the number of elements is null, the result is the entire list, no elements are skipped.
 * If the number of elements is less than zero, the result is an empty list.
 *
 * Tail(argument List<T>) List<T>
 * X.tail() -> Slice(X,1,null)
 * The Tail operator returns all but the first element from the given list.
 * If the list is empty, the result is empty.
 * If the source list is null, the result is null.
 *
 * Take(argument List<T>, number Integer) List<T>
 * X.take(Y) -> Slice(X,0,Y)
 * The Take operator returns the first number of elements from the given list.
 * If the source list has fewer elements than should be taken, the result only contains the elements in the list.
 * If the source list is null, the result is null.
 * If the number is null, or 0 or less, the result is an empty list.
 * */

public class SliceEvaluator {

    public static Object slice(Object source, Integer start, Integer end) {
        if (source == null) {
            return null;
        }
        if (source instanceof Iterable<?> iterable) {
            // Tricky part:
            // Take returns empty list -> Take(List<T>, null) -> start is 0 and end is null
            // Skip returns entire list -> Skip(List<T>, 0) -> start is 0 and end is null
            // Both have the same sig: Slice(List<T>, 0, null)
            //        if (start == null) {
            //            return source;
            //        }
            final List<Object> result = new ArrayList<>();
            int index = 0;
            for (final var it = iterable.iterator(); (end == null || index < end) && it.hasNext(); ++index) {
                final var element = it.next();
                if (start <= index) {
                    result.add(element);
                }
            }
            return result;
        } else {
            throw new InvalidOperatorArgument(
                    "Slice(List<T>, Integer, Integer)",
                    String.format(
                            "Slice(%s, %s, %s)",
                            source.getClass().getName(),
                            start.getClass().getName(),
                            end.getClass().getName()));
        }
    }
}
