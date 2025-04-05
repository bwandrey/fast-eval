package fasteval.perf;


import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class RandomRuleSetGenerator {

    static final Random random = new Random();

    public static void main(String[] args) throws IOException {
        int tokenCount = 50;
        int ruleCount = 20;

        List<String> tokenNames = new ArrayList<>();
        Map<String, String> tokenTypes = new LinkedHashMap<>();
        List<String> ruleLines = new ArrayList<>();

        String[] types = {"int", "double", "boolean", "string"};

        // Generate tokens
        for (int i = 0; i < tokenCount; i++) {
            String name = "t" + i;
            String type = types[random.nextInt(types.length)];
            tokenNames.add(name);
            tokenTypes.put(name, type);
        }

        // Generate rules
        for (int i = 0; i < ruleCount; i++) {
            String ruleName = "r" + i;
            String expression = generateRandomExpression(i, tokenNames, tokenTypes);
            ruleLines.add("    " + ruleName + ": " + expression);
        }

        // Write to file
        try (FileWriter writer = new FileWriter("randrules.txt")) {
            writer.write("tokens:\n");
            for (String token : tokenNames) {
                writer.write("    " + token + ": " + tokenTypes.get(token) + "\n");
            }

            writer.write("\nrules:\n");
            for (String ruleLine : ruleLines) {
                writer.write(ruleLine + "\n");
            }

            writer.write("\ngroups:\n");
            writer.write("    stress:\n");
            for (int i = 0; i < ruleCount; i++) {
                writer.write("        r" + i + "\n");
            }
        }

        System.out.println("Generated rules.txt with " + tokenCount + " tokens and " + ruleCount + " rules.");
    }

    private static String generateRandomExpression(int ruleIndex, List<String> tokenNames, Map<String, String> tokenTypes) {
        StringBuilder sb = new StringBuilder();

        int parts = 2 + random.nextInt(3);
        for (int i = 0; i < parts; i++) {
            if (i > 0) {
                sb.append(" ").append(randomOp()).append(" ");
            }

            if (ruleIndex > 0 && random.nextDouble() < 0.2) {
                // Random rule ref
                int ref = random.nextInt(ruleIndex);
                sb.append("r").append(ref);
            } else {
                // Random comparison
                String token = tokenNames.get(random.nextInt(tokenNames.size()));
                String type = tokenTypes.get(token);
                sb.append(randomComparison(token, type));
            }
        }

        return sb.toString();
    }

    private static String randomOp() {
        return switch (random.nextInt(4)) {
            case 0 -> "AND";
            case 1 -> "OR";
            case 2 -> "XOR";
            default -> "AND";
        };
    }
    private static String randomComparison(String token, String type) {
        switch (type) {
            case "int", "double" -> {
                String op = randomNumericOperator();
                String value = type.equals("int")
                        ? String.valueOf(random.nextInt(100))
                        : String.format("%.2f", random.nextDouble() * 100);
                return token + " " + op + " " + value;
            }
            case "boolean" -> {
                String op = randomEqualityOperator(); // only == / !=
                return token + " " + op + " " + (random.nextBoolean() ? "true" : "false");
            }
            case "string" -> {
                String op = randomEqualityOperator();
                return token + " " + op + " \"" + randomString() + "\"";
            }
            default -> throw new IllegalArgumentException("Unknown type: " + type);
        }
    }



    private static String randomString() {
        String[] pool = {"ALPHA", "BETA", "DELTA", "OMEGA"};
        return pool[random.nextInt(pool.length)];
    }

    private static String randomNumericOperator() {
        String[] ops = {">", "<", ">=", "<=", "==", "!="};
        return ops[random.nextInt(ops.length)];
    }

    private static String randomEqualityOperator() {
        return random.nextBoolean() ? "==" : "!=";
    }

    private static String randomNumericValue(String type) {
        if (type.equals("int")) {
            return String.valueOf(random.nextInt(100));
        } else {
            return String.format("%.2f", random.nextDouble() * 100);
        }
    }

}
