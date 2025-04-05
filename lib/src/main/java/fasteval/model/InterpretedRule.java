package fasteval.model;

import fasteval.context.EvalContext;
import fasteval.eval.RuleNodeEvaluator;

import java.util.Map;

public class InterpretedRule implements RuleRepresentation {

    private final RuleNode rootNode;
    private final Map<String, RuleNode> ruleNodeMap;
    private final Map<String, String> tokenTypeMap;

    public InterpretedRule(RuleNode rootNode,
                           Map<String, RuleNode> ruleNodeMap,
                           Map<String, String> tokenTypeMap) {
        this.rootNode = rootNode;
        this.ruleNodeMap = ruleNodeMap;
        this.tokenTypeMap = tokenTypeMap;
    }

    @Override
    public boolean evaluate(EvalContext context) {
        return new RuleNodeEvaluator(context, ruleNodeMap, tokenTypeMap).evaluate(rootNode);
    }
}