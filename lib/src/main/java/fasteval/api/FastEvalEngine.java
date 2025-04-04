package fasteval.api;

import fasteval.context.EvalContext;
import fasteval.context.RuleContext;
import fasteval.eval.RuleNodeEvaluator;
import fasteval.model.RuleNode;
import fasteval.parser.ExpressionParser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class FastEvalEngine {
    private final RuleContext context;
    private final EvalContext evalContext;

    public FastEvalEngine(RuleContext context, EvalContext evalContext) {
        this.context = context;
        this.evalContext = evalContext;
    }

    public boolean evaluate(String ruleNameOrExpression) {
        RuleNode rule = context.getRuleNodeMap().get(ruleNameOrExpression);

        if (rule == null) {//not a rule, trying to eval it as an expression
            try {
                ExpressionParser parser = new ExpressionParser(
                        new HashSet<>(context.getRuleDefinitionSet()),  // full rule definitions
                        new ArrayList<>(context.getTokenDefinitionMap()) // token definitions
                );
                rule = parser.parse(ruleNameOrExpression);
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid rule name or expression: " + ruleNameOrExpression, e);
            }
        }

        RuleNodeEvaluator evaluator = new RuleNodeEvaluator(evalContext, context.getRuleNodeMap());
        return evaluator.evaluate(rule);
    }

    public List<String> evaluateGroup(String groupName) {
        List<String> ruleNames = context.getRuleGroups().get(groupName);
        if (ruleNames == null) {
            throw new IllegalArgumentException("Group not found: " + groupName);
        }

        List<String> passed = new ArrayList<>();
        RuleNodeEvaluator evaluator = new RuleNodeEvaluator(evalContext, context.getRuleNodeMap());

        for (String ruleName : ruleNames) {
            RuleNode rule = context.getRuleNodeMap().get(ruleName);
            if (rule == null) {
                throw new IllegalStateException("Rule not found: " + ruleName);
            }

            try {
                if (evaluator.evaluate(rule)) {
                    passed.add(ruleName);
                }
            } catch (Exception e) {
                throw new IllegalStateException("Evaluation failed for rule: " + ruleName, e);
            }
        }

        return passed;
    }

}