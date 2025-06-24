package org.opencds.cqf.cql.engine.elm.executing;

import java.util.List;
import org.opencds.cqf.cql.engine.exception.InvalidOperatorArgument;

/*
*** STRING NOTES ***
[](argument String, index Integer) String

The indexer ([]) operator returns the character at the indexth position in a string.
Indexes in strings are defined to be 0-based.
If either argument is null, the result is null.
If the index is greater than the length of the string being indexed, the result is null.

*** LIST NOTES ***
[](argument List<T>, index Integer) T

The indexer ([]) operator returns the element at the indexth position in a list.
Indexes in lists are defined to be 0-based.
If the index is greater than the number of elements in the list, the result is null.
If either argument is null, the result is null.
*/

public class IndexerEvaluator {

    public static Object indexer(Object left, Object right) {
        if (left == null || right == null) {
            return null;
        }

        if (right instanceof Integer index) {
            if (index < 0) {
                return null;
            } else if (left instanceof String string) {
                return index < string.length() ? "" + string.charAt(index) : null;
            } else if (left instanceof List<?> list) {
                // If LEFT is an ArrayList, List.get() will run in O(1) as opposed to O(N) in the following case for
                // Iterable.
                return index < list.size() ? list.get(index) : null;
            } else if (left instanceof Iterable<?> iterable) {
                int i = 0;
                for (Object element : iterable) {
                    if (i == index) {
                        return element;
                    }
                    ++i;
                }
                return null;
            }
        }

        throw new InvalidOperatorArgument(
                "Indexer(String, Integer) or Indexer(List<T>, Integer)",
                String.format(
                        "Indexer(%s, %s)",
                        left.getClass().getName(), right.getClass().getName()));
    }
}
