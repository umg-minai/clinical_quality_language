package org.opencds.cqf.cql.engine.elm.executing;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.opencds.cqf.cql.engine.runtime.*;

/*

Children(argument Any) List<Any>

For structured types, the Children operator returns a list of all the values of the elements of the type.
    List-valued elements are expanded and added to the result individually, rather than as a single list.
For list types, the result is the same as invoking Children on each element in the list and flattening the resulting lists into a single result.
If the source is null, the result is null.

*/

public class ChildrenEvaluator {

    private static void addQuantity(List<Object> list, Quantity quantity) {
        list.add(quantity.getValue());
        list.add(quantity.getUnit());
    }

    private static void addCode(List<Object> list, Code code) {
        list.add(code.getSystem());
        list.add(code.getVersion());
        list.add(code.getCode());
        list.add(code.getSystem());
    }

    private static void addConcept(List<Object> list, Concept concept) {
        for (Code code : concept.getCodes()) {
            addCode(list, code);
        }

        list.add(concept.getDisplay());
    }

    private static void addDateTime(List<Object> list, DateTime dateTime) {
        for (int i = 0; i < dateTime.getPrecision().toDateTimeIndex() + 1; ++i) {
            list.add(dateTime.getDateTime().get(Precision.fromDateTimeIndex(i).toChronoField()));
        }

        list.add(TemporalHelper.zoneToOffset(dateTime.getDateTime().getOffset()));
    }

    private static void addTime(List<Object> list, Time time) {
        for (int i = 0; i < time.getPrecision().toTimeIndex() + 1; ++i) {
            list.add(time.getTime().get(Precision.fromTimeIndex(i).toChronoField()));
        }
    }

    private static void addList(List<Object> list, Iterable<?> listToProcess) {
        for (Object o : listToProcess) {
            list.add(children(o));
        }
    }

    @SuppressWarnings("unchecked")
    public static Object children(Object source) {
        if (source == null) {
            return null;
        }

        List<Object> ret = new ArrayList<>();

        if (source instanceof Integer
                || source instanceof BigDecimal
                || source instanceof String
                || source instanceof Boolean) {
            ret.add(source);
        } else if (source instanceof Quantity quantity) {
            addQuantity(ret, quantity);
        } else if (source instanceof Code code) {
            addCode(ret, code);
        } else if (source instanceof Concept concept) {
            addConcept(ret, concept);
        } else if (source instanceof DateTime dateTime) {
            addDateTime(ret, dateTime);
        } else if (source instanceof Time time) {
            addTime(ret, time);
        } else if (source instanceof Iterable<?> iterable) {
            addList(ret, iterable);
        }

        // TODO: Intervals and Tuples?

        return ret;
    }
}
