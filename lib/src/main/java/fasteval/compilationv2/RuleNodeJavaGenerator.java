package fasteval.compilationv2;

import fasteval.compilation.RuleNodeReferenceFinder;
import fasteval.context.EvalContext;
import fasteval.model.CompiledRule;
import fasteval.model.RuleNode;
import fasteval.model.RuleNode.Type;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class RuleNodeJavaGenerator {

    private final String className;
    private final RuleNode node;
    private final Set<String> referencedRules;

    public RuleNodeJavaGenerator(String className, RuleNode node) {
        this.className = className;
        this.node = node;
        this.referencedRules = RuleNodeReferenceFinder.findReferencedRuleNames(node);
    }

    public String generate() {
        StringBuilder sb = new StringBuilder();

        sb.append("package fasteval.compiled;\n\n")
                .append("import fasteval.model.*;\n")
                .append("import fasteval.context.EvalContext;\n")
                .append("public class ").append(className).append(" implements CompiledRule {\n");

        // Inject referenced fields
        for (String ref : referencedRules) {
            sb.append("  private final CompiledRule ref_").append(ref).append(";\n");
        }

        // Constructor
        sb.append("  public ").append(className).append("(");
        sb.append(referencedRules.stream()
                .map(r -> "CompiledRule ref_" + r)
                .collect(Collectors.joining(", ")));
        sb.append(") {\n");
        for (String ref : referencedRules) {
            sb.append("    this.ref_").append(ref).append(" = ref_").append(ref).append(";\n");
        }
        sb.append("  }\n\n");

        // evaluate method
        sb.append("  @Override public boolean evaluate(EvalContext ctx) {\n");
        sb.append("    return ").append(generateExpression(node)).append(";\n");
        sb.append("  }\n");

        sb.append("}\n");
        return sb.toString();
    }

    private String generateExpression(RuleNode node) {
        return switch (node.getType()) {
            case AND -> "(" + generateExpression(node.getLeft()) + " && " + generateExpression(node.getRight()) + ")";
            case OR -> "(" + generateExpression(node.getLeft()) + " || " + generateExpression(node.getRight()) + ")";
            case XOR -> "(" + generateExpression(node.getLeft()) + " ^ " + generateExpression(node.getRight()) + ")";
            case NOT -> "(!" + generateExpression(node.getLeft()) + ")";
            case RULE_REFERENCE -> "ref_" + node.getReferencedRule().getName() + ".evaluate(ctx)";
            case COMPARISON -> generateComparison(node);
        };
    }

    private String generateComparison(RuleNode node) {
        String token = node.getTokenName();
        String value = node.getValue();
        String op = node.getOperator();

        if (value.equals("true") || value.equals("false")) {
            return "ctx.getBoolean(\"" + token + "\") " + op + " " + Boolean.parseBoolean(value);
        }

        try {
            Integer.parseInt(value);
            return "ctx.getInt(\"" + token + "\") " + op + " " + value;
        } catch (NumberFormatException ignored) {}

        try {
            Double.parseDouble(value);
            return "ctx.getDouble(\"" + token + "\") " + op + " " + value;
        } catch (NumberFormatException ignored) {}

        String escaped = value.replace("\"", "\\\"");
        String expr = "ctx.getString(\"" + token + "\").equals(\"" + escaped + "\")";
        return op.equals("!=") ? "(" + expr + " == false)" : expr;    }
}
