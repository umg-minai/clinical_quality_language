package org.opencds.cqf.cql.engine.elm.executing;

import org.opencds.cqf.cql.engine.exception.InvalidOperatorArgument;

/*
Count(argument List<T>) Integer

* The Count operator returns the number of non-null elements in the source.
* If the list contains no non-null elements, the result is 0.
* If the list is null, the result is null.
* Always returns Integer
*/

public class CountEvaluator {

    public static Object count(Object source) {
        if (source == null) {
            return 0;
        }

        Integer size = 0;
        if (source instanceof Iterable<?> iterable) {
            if (!iterable.iterator().hasNext()) { // empty list
                return size;
            }
            for (var element : iterable) {
                if (element != null) { // skip null
                    ++size;
                }
            }
            return size;
        }

        throw new InvalidOperatorArgument(
                "Count(List<T>)", String.format("Count(%s)", source.getClass().getName()));
    }
}
