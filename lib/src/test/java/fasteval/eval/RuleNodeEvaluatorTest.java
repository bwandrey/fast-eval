package fasteval.eval;

import fasteval.context.EvalContext;
import fasteval.context.EvalContextImpl;
import fasteval.definitions.RuleDefinition;
import fasteval.model.RuleNode;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class RuleNodeEvaluatorTest {

    private EvalContext makeContext(Object... keyVals) {
        EvalContext ctx = new EvalContextImpl();
        for (int i = 0; i < keyVals.length; i += 2) {
            String key = (String) keyVals[i];
            Object value = keyVals[i + 1];
            if (value instanceof Double d) ctx.withDouble(key, d);
            else if (value instanceof Integer in) ctx.withInt(key, in);
            else if (value instanceof Boolean b) ctx.withBoolean(key, b);
            else if (value instanceof String s) ctx.withString(key, s);
            else throw new IllegalArgumentException("Unsupported type: " + value.getClass());
        }
        return ctx;
    }

    @Test
    public void testDoubleComparison() {
        RuleNode node = RuleNode.comparison("stockPrice", ">", "100.0");
        EvalContext ctx = makeContext("stockPrice", 120.0);
        boolean result = new RuleNodeEvaluator(ctx, Map.of()).evaluate(node);
        assertTrue(result);
    }

    @Test
    public void testIntComparison() {
        RuleNode node = RuleNode.comparison("age", "<", "30");
        EvalContext ctx = makeContext("age", 25);
        boolean result = new RuleNodeEvaluator(ctx, Map.of()).evaluate(node);
        assertTrue(result);
    }

    @Test
    public void testBooleanComparison() {
        RuleNode node = RuleNode.comparison("isOpen", "==", "true");
        EvalContext ctx = makeContext("isOpen", true);
        boolean result = new RuleNodeEvaluator(ctx, Map.of()).evaluate(node);
        assertTrue(result);
    }

    @Test
    public void testStringComparison() {
        RuleNode node = RuleNode.comparison("status", "!=", "CLOSED");
        EvalContext ctx = makeContext("status", "OPEN");
        boolean result = new RuleNodeEvaluator(ctx, Map.of()).evaluate(node);
        assertTrue(result);
    }

    @Test
    public void testAndOperator() {
        RuleNode left = RuleNode.comparison("a", "==", "true");
        RuleNode right = RuleNode.comparison("b", "==", "true");
        RuleNode node = RuleNode.logical(RuleNode.Type.AND, left, right);
        EvalContext ctx = makeContext("a", true, "b", true);
        assertTrue(new RuleNodeEvaluator(ctx, Map.of()).evaluate(node));
    }

    @Test
    public void testOrOperator() {
        RuleNode left = RuleNode.comparison("x", "==", "false");
        RuleNode right = RuleNode.comparison("y", "==", "true");
        RuleNode node = RuleNode.logical(RuleNode.Type.OR, left, right);
        EvalContext ctx = makeContext("x", false, "y", true);
        assertTrue(new RuleNodeEvaluator(ctx, Map.of()).evaluate(node));
    }

    @Test
    public void testXorOperator() {
        RuleNode left = RuleNode.comparison("x", "==", "true");
        RuleNode right = RuleNode.comparison("y", "==", "true");
        RuleNode node = RuleNode.logical(RuleNode.Type.XOR, left, right);
        EvalContext ctx = makeContext("x", false, "y", true);
        assertTrue(new RuleNodeEvaluator(ctx, Map.of()).evaluate(node));

    }

    @Test
    public void testNotOperator() {
        RuleNode inner = RuleNode.comparison("enabled", "==", "true");
        RuleNode node = RuleNode.not(inner);
        EvalContext ctx = makeContext("enabled", false);
        assertTrue(new RuleNodeEvaluator(ctx, Map.of()).evaluate(node));
    }

    @Test
    public void testRuleReference() {
        RuleDefinition def = new RuleDefinition("isHigh", "ignored"); // expression not needed here
        RuleNode refNode = RuleNode.ruleRef(def);
        RuleNode realNode = RuleNode.comparison("value", ">", "10");
        EvalContext ctx = makeContext("value", 15);

        boolean result = new RuleNodeEvaluator(ctx, Map.of(
                "isHigh", realNode
        )).evaluate(refNode);

        assertTrue(result);
    }

    @Test
    public void testMissingRuleReferenceThrows() {
        RuleDefinition def = new RuleDefinition("missingRule", "ignored");
        RuleNode refNode = RuleNode.ruleRef(def);
        EvalContext ctx = makeContext();

        Exception e = assertThrows(RuntimeException.class, () -> {
            new RuleNodeEvaluator(ctx, Map.of()).evaluate(refNode);
        });

        assertTrue(e.getMessage().contains("Referenced rule not found"));
    }

    @Test
    public void testInvalidOperatorThrows() {
        RuleNode node = RuleNode.comparison("x", "!!", "true");
        EvalContext ctx = makeContext("x", true);

        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            new RuleNodeEvaluator(ctx, Map.of()).evaluate(node);
        });

        assertTrue(e.getMessage().contains("Invalid boolean operator"));
    }
}
