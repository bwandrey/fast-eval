package fasteval.eval;

import fasteval.context.EvalContext;
import fasteval.definitions.RuleDefinition;
import fasteval.model.RuleNode;

import java.util.Map;

public class RuleNodeEvaluator {

    private final EvalContext context;
    private final Map<String, RuleNode> ruleNodeMap;
    private final Map<String, String> tokenTypeMap;

    public RuleNodeEvaluator(EvalContext context,
                             Map<String, RuleNode> ruleNodeMap,
                             Map<String, String> tokenTypeMap) {
        this.context = context;
        this.ruleNodeMap = ruleNodeMap;
        this.tokenTypeMap = tokenTypeMap;
    }

    public boolean evaluate(RuleNode node) {
        switch (node.getType()) {
            case COMPARISON:
                return evalComparison(node);
            case RULE_REFERENCE:
                RuleDefinition referencedDef = node.getReferencedRule();
                RuleNode referencedNode = ruleNodeMap.get(referencedDef.getName());
                if (referencedNode == null) {
                    throw new RuntimeException("Referenced rule not found: " + referencedDef.getName());
                }
                return evaluate(referencedNode);
            case AND:
                return evaluate(node.getLeft()) && evaluate(node.getRight());
            case OR:
                return evaluate(node.getLeft()) || evaluate(node.getRight());
            case XOR:
                return evaluate(node.getLeft()) ^ evaluate(node.getRight());
            case NOT:
                return !evaluate(node.getLeft());
            default:
                throw new IllegalStateException("Unsupported RuleNode type: " + node.getType());
        }
    }

    private boolean evalComparison(RuleNode node) {
        String token = node.getTokenName();
        String operator = node.getOperator();
        String valueStr = node.getValue();

        String expectedType = tokenTypeMap.get(token);
        if (expectedType == null) {
            throw new IllegalArgumentException("Token type not found for: " + token);
        }

        Object value = getTokenValue(token, expectedType);

        switch (expectedType.toLowerCase()) {
            case "double":
                return compare((Double) value, Double.parseDouble(valueStr.replaceAll(",", ".")), operator);
            case "int":
                return compare((Integer) value, Integer.parseInt(valueStr), operator);
            case "boolean":
                return compare((Boolean) value, Boolean.parseBoolean(valueStr), operator);
            case "string":
                return compare((String) value, valueStr, operator);
            default:
                throw new IllegalArgumentException("Unsupported token type: " + expectedType);
        }
    }

    private Object getTokenValue(String token, String expectedType) {
        return switch (expectedType.toLowerCase()) {
            case "boolean" -> context.getBoolean(token);
            case "int" -> context.getInt(token);
            case "double" -> context.getDouble(token);
            case "string" -> context.getString(token);
            default -> throw new IllegalArgumentException("Unknown token type: " + expectedType);
        };
    }

    private boolean compare(double left, double right, String op) {
        return switch (op) {
            case ">" -> left > right;
            case "<" -> left < right;
            case "==" -> left == right;
            case "!=" -> left != right;
            case ">=" -> left >= right;
            case "<=" -> left <= right;
            default -> throw new IllegalArgumentException("Invalid operator: " + op);
        };
    }

    private boolean compare(int left, int right, String op) {
        return compare((double) left, (double) right, op);
    }

    private boolean compare(boolean left, boolean right, String op) {
        return switch (op) {
            case "==" -> left == right;
            case "!=" -> left != right;
            default -> throw new IllegalArgumentException("Invalid boolean operator: " + op);
        };
    }

    private boolean compare(String left, String right, String op) {
        return switch (op) {
            case "==" -> left.equals(right);
            case "!=" -> !left.equals(right);
            default -> throw new IllegalArgumentException("Invalid string operator: " + op);
        };
    }
}
