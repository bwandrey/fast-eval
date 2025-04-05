package fasteval.perf;

import fasteval.api.FastEval;
import fasteval.api.FastEvalBuilder;
import fasteval.api.FastEvalEngine;
import fasteval.context.EvalContext;
import fasteval.parser.ExpressionParser;
import fasteval.parser.TextFileParser;

import java.io.IOException;
import java.util.*;

public class PerfTestMain {

    public static void main(String[] args) throws IOException, InterruptedException {
        final int WARMUP = 1000_000;
        final int RUNS = 1_000_000;
        final String ruleToTest = "r1999"; // or whatever your last/top rule is

        System.out.println("Loading rule set...");
        TextFileParser parser = new TextFileParser();
        parser.parseRulesFile("randrules.txt");

        ExpressionParser expressionParser = new ExpressionParser(
                new HashSet<>(parser.getRules()),
                parser.getTokens()
        );

        EvalContext ctx = randomContextFromParser(parser);

        // === Interpreted ===
        FastEvalBuilder interpreted = new FastEvalBuilder(expressionParser,
                parser.getRules(),
                parser.getTokens(),
                parser.getGroups());

        interpreted.usingInterpretation();
        FastEvalEngine engineInterp = FastEval.withEvaluationContext(ctx);

        System.out.println("Warming up interpreted...");
        for (int i = 0; i < WARMUP; i++) engineInterp.evaluate(ruleToTest);

        System.out.println("Running interpreted...");
        long t0 = System.nanoTime();
        for (int i = 0; i < RUNS; i++) {
            EvalContext eval = randomContextFromParser(parser);
            FastEval.withEvaluationContext(eval).evaluate(ruleToTest);
        }
        long t1 = System.nanoTime();
        long timeInterp = (t1 - t0) / 1_000_000;
        System.out.println("Interpreted: " + timeInterp + " ms");

        // === String Compiled ===
        FastEvalBuilder compiled = new FastEvalBuilder(expressionParser,
                parser.getRules(),
                parser.getTokens(),
                parser.getGroups());

        compiled.usingStringCompilation();
        FastEvalEngine engineCompiled = FastEval.withEvaluationContext(ctx);

        System.out.println("Warming up compiled...");
        for (int i = 0; i < WARMUP; i++) engineCompiled.evaluate(ruleToTest);

        System.out.println("Running compiled...");
        long t2 = System.nanoTime();
        for (int i = 0; i < RUNS; i++) {
            EvalContext eval = randomContextFromParser(parser);
            FastEval.withEvaluationContext(eval).evaluate(ruleToTest);
        }
        long t3 = System.nanoTime();
        long timeCompiled = (t3 - t2) / 1_000_000;
        System.out.println("Compiled: " + timeCompiled + " ms");

        System.out.println();
        System.out.println("Speedup: " + ((double) timeInterp / timeCompiled) + "x");
    }

    private static EvalContext randomContextFromParser(TextFileParser parser) {
        Map<String, String> tokenTypes = new HashMap<>();
        parser.getTokens().forEach(td -> tokenTypes.put(td.getName(), td.getType()));
        return new RandomizingEvalContext(tokenTypes);
    }

    public static class RandomizingEvalContext implements EvalContext {
        private final Map<String, String> tokenTypes;
        private final Random random = new Random();

        public RandomizingEvalContext(Map<String, String> tokenTypes) {
            this.tokenTypes = tokenTypes;
        }

        @Override public boolean getBoolean(String token) {
            return random.nextBoolean();
        }

        @Override public int getInt(String token) {
            return random.nextInt(1000);
        }

        @Override public double getDouble(String token) {
            return random.nextDouble() * 1000;
        }

        @Override public String getString(String token) {
            String[] pool = {"ALPHA", "BETA", "OMEGA", "ZETA"};
            return pool[random.nextInt(pool.length)];
        }

        // These are unused in this test
        @Override public EvalContext withDouble(String tokenName, double value) { return this; }
        @Override public EvalContext withInt(String tokenName, int value) { return this; }
        @Override public EvalContext withBoolean(String tokenName, boolean value) { return this; }
        @Override public EvalContext withString(String tokenName, String value) { return this; }
    }
}
