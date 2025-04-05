package fasteval.api;

import fasteval.context.EvalContext;
import fasteval.context.RuleContextInterface;
import fasteval.model.RuleRepresentation;

import java.util.ArrayList;
import java.util.List;
public class FastEvalEngine {
    private final RuleContextInterface context;
    private final EvalContext evalContext;

    public FastEvalEngine(RuleContextInterface context, EvalContext evalContext) {
        this.context = context;
        this.evalContext = evalContext;
    }

    public boolean evaluate(String ruleName) {
        RuleRepresentation rule = context.getRules().get(ruleName);
        if (rule == null) {
            throw new IllegalArgumentException("Rule not found: " + ruleName);
        }
        return rule.evaluate(evalContext);
    }

    public List<String> evaluateGroup(String groupName) {
        List<String> ruleNames = context.getRuleGroups().get(groupName);
        if (ruleNames == null) {
            throw new IllegalArgumentException("Group not found: " + groupName);
        }

        List<String> passed = new ArrayList<>();
        for (String ruleName : ruleNames) {
            RuleRepresentation rule = context.getRules().get(ruleName);
            if (rule == null) {
                throw new IllegalArgumentException("Rule not found: " + ruleName);
            }
            if (rule.evaluate(evalContext)) {
                passed.add(ruleName);
            }
        }
        return passed;
    }
}
