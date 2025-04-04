package fasteval.api;

import fasteval.context.EvalContext;
import fasteval.context.EvalContextImpl;
import fasteval.context.RuleContext;
import fasteval.definitions.RuleDefinition;
import fasteval.definitions.TokenDefinition;
import fasteval.model.RuleNode;
import fasteval.parser.ExpressionParser;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class FastEvalEngineGroupTest {

    private RuleContext buildRuleContext() {
        Set<TokenDefinition> tokens = Set.of(
                new TokenDefinition("stockPrice", "double"),
                new TokenDefinition("stockHalted", "boolean")
        );

        Set<RuleDefinition> rules = Set.of(
                new RuleDefinition("priceHigh", "stockPrice > 100.0"),
                new RuleDefinition("stockStopped", "stockHalted == true"),
                new RuleDefinition("criticalCondition", "priceHigh AND stockStopped"),
                new RuleDefinition("criticalConditionTwo", "priceHigh AND stockHalted")
        );

        Map<String, List<String>> groups = Map.of(
                "criticalSet", List.of("priceHigh", "stockStopped", "criticalCondition")
        );

        ExpressionParser parser = new ExpressionParser(rules, new ArrayList<>(tokens));
        Map<String, RuleNode> ruleMap = parser.parseAllToMap();

        return new RuleContext(rules, tokens, ruleMap, groups);
    }

    private EvalContext ctx(Object... kv) {
        EvalContextImpl ctx = new EvalContextImpl();
        for (int i = 0; i < kv.length; i += 2) {
            String key = (String) kv[i];
            Object value = kv[i + 1];
            if (value instanceof Double d) ctx.withDouble(key, d);
            else if (value instanceof Integer in) ctx.withInt(key, in);
            else if (value instanceof Boolean b) ctx.withBoolean(key, b);
            else if (value instanceof String s) ctx.withString(key, s);
        }
        return ctx;
    }

    @Test
    public void testEvaluateGroup_AllPass() {
        FastEvalEngine engine = new FastEvalEngine(buildRuleContext(),
                ctx("stockPrice", 150.0, "stockHalted", true));

        List<String> result = engine.evaluateGroup("criticalSet");

        assertEquals(Set.of("priceHigh", "stockStopped", "criticalCondition"), Set.copyOf(result));
    }

    @Test
    public void testEvaluateGroup_SomeFail() {
        FastEvalEngine engine = new FastEvalEngine(buildRuleContext(),
                ctx("stockPrice", 90.0, "stockHalted", true));

        List<String> result = engine.evaluateGroup("criticalSet");

        assertEquals(List.of("stockStopped"), result);  // only this rule passes
    }

    @Test
    public void testEvaluateGroup_MissingGroup() {
        FastEvalEngine engine = new FastEvalEngine(buildRuleContext(),
                ctx("stockPrice", 150.0, "stockHalted", true));

        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                engine.evaluateGroup("nonexistentGroup"));

        assertTrue(ex.getMessage().contains("Group not found"));
    }

    @Test
    public void testEvaluateGroup_MissingTokenThrows() {
        FastEvalEngine engine = new FastEvalEngine(buildRuleContext(),
                ctx("stockHalted", true)); // missing stockPrice

        Exception ex = assertThrows(IllegalStateException.class, () ->
                engine.evaluateGroup("criticalSet"));

        assertTrue(ex.getMessage().contains("Evaluation failed for rule"));
    }

    @Test
    public void testEvaluateGroup_UnknownRuleInGroupThrows() {
        RuleContext context = buildRuleContext();
        context.getRuleGroups().put("invalidGroup", List.of("nonexistentRule"));

        FastEvalEngine engine = new FastEvalEngine(context,
                ctx("stockPrice", 150.0, "stockHalted", true));

        Exception ex = assertThrows(IllegalStateException.class, () ->
                engine.evaluateGroup("invalidGroup"));

        assertTrue(ex.getMessage().contains("Rule not found"));
    }
}
