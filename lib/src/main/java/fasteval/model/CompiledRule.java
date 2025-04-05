package fasteval.model;

import fasteval.context.EvalContext;

public interface CompiledRule extends RuleRepresentation {
    @Override
    boolean evaluate(EvalContext context);
}