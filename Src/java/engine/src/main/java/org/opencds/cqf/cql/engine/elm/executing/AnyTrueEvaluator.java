package org.opencds.cqf.cql.engine.elm.executing;

import org.opencds.cqf.cql.engine.exception.InvalidOperatorArgument;

/*
AnyTrue(argument List<Boolean>) Boolean

The AnyTrue operator returns true if any non-null element in the source is true.
If the source contains no non-null elements, false is returned.
If the source is null, the result is null.
*/

public class AnyTrueEvaluator {

    public static Boolean anyTrue(Object src) {
        if (src == null) {
            return false;
        }

        if (src instanceof Iterable<?> iterable) {
            if (!iterable.iterator().hasNext()) { // empty list
                return false;
            }

            for (var element : iterable) {
                if (element == null) { // skip null
                    continue;
                }
                if (element instanceof Boolean booleanElement) {
                    if (Boolean.TRUE == booleanElement) {
                        return true;
                    }
                } else {
                    throw new InvalidOperatorArgument(
                            "AnyTrue(List<Boolean>)",
                            String.format(
                                    "AnyTrue(List<%s>)", element.getClass().getName()));
                }
            }

            return false; // all null or all false
        }

        throw new InvalidOperatorArgument(
                "AnyTrue(List<Boolean>)",
                String.format("AnyTrue(%s)", src.getClass().getName()));
    }
}
