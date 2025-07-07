package org.opencds.cqf.cql.engine.elm.executing;

import org.opencds.cqf.cql.engine.exception.InvalidOperatorArgument;
import org.opencds.cqf.cql.engine.execution.State;
import org.opencds.cqf.cql.engine.runtime.BaseTemporal;
import org.opencds.cqf.cql.engine.runtime.Interval;

/*
    There are two overloads of this operator:
        List, T: The type of T must be the same as the element type of the list.
        Interval, T : The type of T must be the same as the point type of the interval.

    For the List, T overload, this operator returns true if the given element is in the list,
        and it is not the only element in the list, using equality semantics, with the exception
        that null elements are considered equal.
        If the first argument is null, the result is false.
        If the second argument is null, the result is true if the list contains any null elements
        and at least one other element, and false otherwise.
    For the Interval, T overload, this operator returns true if the given point is greater than
        the starting point of the interval, and less than the ending point of the interval, as
        determined by the Start and End operators.
        If precision is specified and the point type is a Date, DateTime, or Time type, comparisons
        used in the operation are performed at the specified precision.
        If the first argument is null, the result is false.
        If the second argument is null, the result is null.
*/

public class ProperContainsEvaluator {

    public static Boolean properContains(Object left, Object right, State state) {

        // If the first argument is null, the result is false.
        if (left == null) {
            return false;
        }

        if (left instanceof Interval leftInterval) {
            Boolean startProperContains = GreaterEvaluator.greater(right, leftInterval.getStart(), state);
            Boolean endProperContains = LessEvaluator.less(right, leftInterval.getEnd(), state);

            return startProperContains == null
                    ? null
                    : endProperContains == null ? null : startProperContains && endProperContains;
        } else if (left instanceof Iterable<?> leftIterable) {
            // The result cannot be true if the list contains fewer than two elements
            final var iterator = leftIterable.iterator();
            if (!iterator.hasNext()) {
                return false;
            } else {
                iterator.next();
                if (!iterator.hasNext()) {
                    return false;
                }
            }

            if (right == null) {
                // The result is true if the list contains any null elements and at least one other element,
                // and false otherwise
                boolean listContainsNullElements = false;
                boolean listContainsOtherElements = false;
                for (Object element : leftIterable) {
                    if (element == null) {
                        listContainsNullElements = true;
                        if (listContainsOtherElements) {
                            break;
                        }
                    } else {
                        listContainsOtherElements = true;
                        if (listContainsNullElements) {
                            break;
                        }
                    }
                }
                return listContainsNullElements && listContainsOtherElements;
            }

            // Return true if the given element is in the list, and it is not the only element in the list,
            // using equality semantics
            boolean listContainsGivenElement = false;
            boolean listContainsOtherElements = false;
            boolean listContainsElementsOfUnknownEquality = false;

            for (var element : leftIterable) {
                final var equalResult = EqualEvaluator.equal(element, right, state);
                if (equalResult == null) {
                    listContainsElementsOfUnknownEquality = true;
                } else if (equalResult) {
                    listContainsGivenElement = true;
                    if (listContainsOtherElements) {
                        break;
                    }
                } else {
                    listContainsOtherElements = true;
                    if (listContainsGivenElement) {
                        break;
                    }
                }
            }

            // The given element is in the list and there are other elements, using equality semantics
            if (listContainsGivenElement && listContainsOtherElements) {
                return true;
            }

            // The above is false, but there are elements of unknown equality
            if (listContainsElementsOfUnknownEquality) {
                return null;
            }

            return false;
        }

        throw new InvalidOperatorArgument(
                "ProperContains(List<T>, T) or ProperContains(Interval<T>, T)",
                String.format(
                        "ProperContains(%s, %s)",
                        left.getClass().getName(), right.getClass().getName()));
    }

    public static Boolean properContains(Object left, Object right, String precision, State state) {
        if (left instanceof Interval leftInterval && right instanceof BaseTemporal) {
            Boolean startProperContains = AfterEvaluator.after(right, leftInterval.getStart(), precision, state);
            Boolean endProperContains = BeforeEvaluator.before(right, leftInterval.getEnd(), precision, state);

            return startProperContains == null
                    ? null
                    : endProperContains == null ? null : startProperContains && endProperContains;
        }

        return properContains(left, right, state);
    }
}
