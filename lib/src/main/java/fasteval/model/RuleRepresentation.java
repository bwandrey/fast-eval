package fasteval.model;


import fasteval.context.EvalContext;

public interface RuleRepresentation {
    boolean evaluate(EvalContext context);
}