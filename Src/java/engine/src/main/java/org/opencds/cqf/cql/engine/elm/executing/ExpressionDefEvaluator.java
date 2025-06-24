package org.opencds.cqf.cql.engine.elm.executing;

import org.cqframework.cql.elm.visiting.ElmLibraryVisitor;
import org.hl7.elm.r1.ExpressionDef;
import org.hl7.elm.r1.VersionedIdentifier;
import org.opencds.cqf.cql.engine.execution.ExpressionResult;
import org.opencds.cqf.cql.engine.execution.State;

public class ExpressionDefEvaluator {
    public static Object internalEvaluate(
            ExpressionDef expressionDef, State state, ElmLibraryVisitor<Object, State> visitor) {
        final var cache = state.getCache();
        if (cache.isExpressionCachingEnabled()) {
            VersionedIdentifier libraryId = state.getCurrentLibrary().getIdentifier();
            if (cache.isExpressionCached(libraryId, expressionDef.getName())) {
                final var result = cache.getCachedExpression(libraryId, expressionDef.getName());

                // TODO(jmoringe): make public interface
                final var frame = state.getTopActivationFrame();
                assert frame.element == expressionDef;
                frame.isCached = true;

                return result.value();
            } else {
                state.pushEvaluatedResourceStack();
                try {
                    final var value = evaluateWithoutCache(expressionDef, state, visitor);
                    final var result = new ExpressionResult(value, state.getEvaluatedResources());
                    state.getCache().cacheExpression(libraryId, expressionDef.getName(), result);
                    return value;
                } finally {
                    state.popEvaluatedResourceStack();
                }
            }
        } else {
            return evaluateWithoutCache(expressionDef, state, visitor);
        }
    }

    private static Object evaluateWithoutCache(
            final ExpressionDef expressionDef, final State state, final ElmLibraryVisitor<Object, State> visitor) {
        boolean isEnteredContext = false;
        if (expressionDef.getContext() != null) {
            isEnteredContext = state.enterContext(expressionDef.getContext());
        }
        try {
            return visitor.visitExpression(expressionDef.getExpression(), state);
        } finally {
            // state.enterContext.getContext() == null will result in isEnteredContext = false, which means pop() won't
            // be called
            state.exitContext(isEnteredContext);
        }
    }
}
