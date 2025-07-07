package org.opencds.cqf.cql.engine.elm.executing;

import org.opencds.cqf.cql.engine.exception.InvalidOperatorArgument;

/*
AllTrue(argument List<Boolean>) Boolean

The AllTrue operator returns true if all the non-null elements in the source are true.
If the source contains no non-null elements, true is returned.
If the source is null, the result is null.
*/

public class AllTrueEvaluator {

    public static Boolean allTrue(Object src) {
        if (src == null) {
            return true;
        }

        if (src instanceof Iterable<?> iterable) {
            if (!iterable.iterator().hasNext()) { // empty list
                return true;
            }

            for (var element : iterable) {
                if (element == null) { // skip null
                    continue;
                }
                if (element instanceof Boolean booleanElement) {
                    if (!booleanElement) {
                        return false;
                    }
                } else {
                    throw new InvalidOperatorArgument(
                            "AllTrue(List<Boolean>)",
                            String.format(
                                    "AllTrue(List<%s>)", element.getClass().getName()));
                }
            }
            return true;
        }

        throw new InvalidOperatorArgument(
                "AllTrue(List<Boolean>)",
                String.format("AllTrue(%s)", src.getClass().getName()));
    }
}
