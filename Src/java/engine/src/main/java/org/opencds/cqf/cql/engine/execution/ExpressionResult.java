package org.opencds.cqf.cql.engine.execution;

import java.util.Set;

public class ExpressionResult {
    protected Object value;
    /**
     * Either a (possibly empty) set of evaluated resources or {@code
     * null} if tracking of evaluated resources was not enabled.
     *
     * @see CqlEngine.Options
     */
    protected Set<Object> evaluatedResources;

    public ExpressionResult(final Object value, final Set<Object> evaluatedResources) {
        this.value = value;
        this.evaluatedResources = evaluatedResources;
    }

    public Object value() {
        return value;
    }

    public Set<Object> evaluatedResources() {
        return this.evaluatedResources;
    }
}
