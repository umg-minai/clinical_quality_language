package org.opencds.cqf.cql.engine.elm.executing;

import org.opencds.cqf.cql.engine.exception.InvalidOperatorArgument;

/*
Combine(source List<String>) String
Combine(source List<String>, separator String) String

The Combine operator combines a list of strings, optionally separating each string with the given separator.
If either argument is null, or any element in the source list of strings is null, the result is null.
*/

public class CombineEvaluator {

    public static Object combine(Object source, String separator) {

        if (source == null || separator == null) {
            return null;
        } else {
            if (source instanceof Iterable<?> iterable) {
                StringBuilder buffer = new StringBuilder();
                boolean first = true;
                for (var element : iterable) {
                    if (element == null) {
                        return null;
                    }

                    if (element instanceof String string) {
                        if (!first) {
                            buffer.append(separator);
                        } else {
                            first = false;
                        }
                        buffer.append(string);
                    } else {
                        throw new InvalidOperatorArgument(
                                "Combine(List<String>) or Combine(List<String>, String)",
                                String.format(
                                        "Combine(List<%s>%s)",
                                        element.getClass().getName(), separator.isEmpty() ? "" : ", " + separator));
                    }
                }
                return buffer.toString();
            }
        }

        throw new InvalidOperatorArgument(
                "Combine(List<String>) or Combine(List<String>, String)",
                String.format(
                        "Combine(%s%s)", source.getClass().getName(), separator.isEmpty() ? "" : ", " + separator));
    }
}
