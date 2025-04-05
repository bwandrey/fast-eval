
package fasteval.api;

import fasteval.context.EvalContext;
import fasteval.context.ObjectRuleContext;
import fasteval.context.RuleContextInterface;
import fasteval.definitions.RuleDefinition;
import fasteval.definitions.TokenDefinition;
import fasteval.parser.ExpressionParser;
import fasteval.parser.TextFileParser;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class FastEval {
    private static RuleContextInterface ruleContext;

    // Called once at startup - returns a builder to allow interpretation/compilation choice
    public static FastEvalBuilder loadFromFile(String filePath) throws IOException {
        TextFileParser parser = new TextFileParser();
        parser.parseRulesFile(filePath);

        ExpressionParser expressionParser = new ExpressionParser(
                new HashSet<>(parser.getRules()),
                parser.getTokens()
        );

        List<RuleDefinition> rules = parser.getRules();
        List<TokenDefinition> tokens = parser.getTokens();
        Map<String, List<String>> groups = parser.getGroups();

        return new FastEvalBuilder(expressionParser, rules, tokens, groups);
    }

    // Called at runtime with tokens
    public static FastEvalEngine withEvaluationContext(EvalContext ctx) {
        if (ruleContext == null) {
            throw new IllegalStateException("FastEval.loadFromFile(...) must be called before using the evaluator.");
        }
        return new FastEvalEngine(ruleContext, ctx);
    }

    // Set rule context (used internally by builder)
    static void setGlobalContext(RuleContextInterface ctx) {
        ruleContext = ctx;
    }
}
