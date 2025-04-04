package fasteval.api;

import fasteval.context.EvalContext;
import fasteval.context.EvalContextImpl;
import fasteval.context.RuleContext;
import fasteval.definitions.RuleDefinition;
import fasteval.definitions.TokenDefinition;
import fasteval.model.RuleNode;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class FastEvalEngineTest {

    private RuleContext createRuleContext() {
        // Define tokens
        List<TokenDefinition> tokens = List.of(
                new TokenDefinition("stockPrice", "double"),
                new TokenDefinition("stockHalted", "boolean")
        );

        // Define rules
        Set<RuleDefinition> rules = Set.of(
                new RuleDefinition("priceHigh", "stockPrice > 100.0"),
                new RuleDefinition("halted", "stockHalted == true")
        );

        // Parse rules into ASTs
        fasteval.parser.ExpressionParser parser = new fasteval.parser.ExpressionParser(rules, tokens);
        Map<String, RuleNode> ruleMap = parser.parseAllToMap();

        return new RuleContext(rules, Set.copyOf(tokens), ruleMap, Map.of());
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
    public void testEvaluateRuleByName() {
        RuleContext ruleContext = createRuleContext();
        EvalContext evalContext = ctx("stockPrice", 120.0, "stockHalted", false);

        FastEvalEngine engine = new FastEvalEngine(ruleContext, evalContext);

        assertTrue(engine.evaluate("priceHigh"));
        assertFalse(engine.evaluate("halted"));
        assertFalse(engine.evaluate("stockHalted"));
    }

    @Test
    public void testEvaluateInlineExpression() {
        RuleContext ruleContext = createRuleContext();
        EvalContext evalContext = ctx("stockPrice", 75.0, "stockHalted", true);

        FastEvalEngine engine = new FastEvalEngine(ruleContext, evalContext);

        assertTrue(engine.evaluate("stockPrice < 100 AND stockHalted"));
    }

    @Test
    public void testEvaluateNestedExpression() {
        RuleContext ruleContext = createRuleContext();
        EvalContext evalContext = ctx("stockPrice", 105.0, "stockHalted", true);

        FastEvalEngine engine = new FastEvalEngine(ruleContext, evalContext);

        assertTrue(engine.evaluate("(priceHigh AND stockHalted)"));
    }

    @Test
    public void testInvalidExpressionThrows() {
        RuleContext ruleContext = createRuleContext();
        EvalContext evalContext = ctx("stockPrice", 105.0);

        FastEvalEngine engine = new FastEvalEngine(ruleContext, evalContext);

        Exception e = assertThrows(IllegalArgumentException.class, () ->
                engine.evaluate("stockPrice >>> 100")
        );

        assertTrue(e.getMessage().contains("Invalid rule name or expression"));
    }

    @Test
    public void testUnknownRuleNameThrows() {
        RuleContext ruleContext = createRuleContext();
        EvalContext evalContext = ctx("stockPrice", 120.0);

        FastEvalEngine engine = new FastEvalEngine(ruleContext, evalContext);

        Exception e = assertThrows(IllegalArgumentException.class, () ->
                engine.evaluate("someMissingRule")
        );

        assertTrue(e.getMessage().contains("Invalid rule name or expression"));
    }
}
