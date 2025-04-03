package fasteval.api;


import fasteval.TextFileParser;
import fasteval.context.EvalContext;
import fasteval.context.RuleContext;
import fasteval.parser.ExpressionParser;

import java.io.IOException;
import java.util.HashSet;

public class FastEval {
    private static RuleContext globalContext;

    // Called once at startup
    public static void loadFromFile(String filePath) throws IOException {
        TextFileParser parser = new TextFileParser();
        parser.parseRulesFile(filePath);

        ExpressionParser expressionParser = new ExpressionParser(
                new HashSet<>(parser.getRules()),
                parser.getTokens()
        );
        globalContext = expressionParser.generateRuleContext();
    }

    // Called at runtime with tokens
    public static FastEvalEngine withEvaluationContext(EvalContext ctx) {
        if (globalContext == null) {
            throw new IllegalStateException("FastEval.loadFromFile(...) must be called before using the evaluator.");
        }
        return new FastEvalEngine(globalContext, ctx);
    }
}