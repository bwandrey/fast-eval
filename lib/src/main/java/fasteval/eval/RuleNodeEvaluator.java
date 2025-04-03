package fasteval.eval;

import fasteval.context.EvalContext;
import fasteval.definitions.RuleDefinition;
import fasteval.model.RuleNode;

import java.util.Map;

public class RuleNodeEvaluator {

    private final EvalContext context;
    private final Map<String, RuleNode> ruleNodeMap; // all rules resolved to ASTs

    public RuleNodeEvaluator(EvalContext context, Map<String, RuleNode> ruleNodeMap) {
        this.context = context;
        this.ruleNodeMap = ruleNodeMap;
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

        Object value = getTokenValue(token);

        if (value instanceof Double) {
            double left = (Double) value;
            double right = Double.parseDouble(valueStr);
            return compare(left, right, operator);
        } else if (value instanceof Integer) {
            int left = (Integer) value;
            int right = Integer.parseInt(valueStr);
            return compare(left, right, operator);
        } else if (value instanceof Boolean) {
            boolean left = (Boolean) value;
            boolean right = Boolean.parseBoolean(valueStr);
            return compare(left, right, operator);
        } else if (value instanceof String) {
            String left = (String) value;
            return compare(left, valueStr, operator);
        } else {
            throw new IllegalArgumentException("Unsupported token type: " + value.getClass());
        }
    }

    private Object getTokenValue(String token) {
        try {
            return context.getBoolean(token);
        } catch (Exception ignored) {}
        try {
            return context.getDouble(token);
        } catch (Exception ignored) {}
        try {
            return context.getInt(token);
        } catch (Exception ignored) {}
        return context.getString(token); // fallback
    }

    private boolean compare(double left, double right, String op) {
        switch (op) {
            case ">": return left > right;
            case "<": return left < right;
            case "==": return left == right;
            case "!=": return left != right;
            case ">=": return left >= right;
            case "<=": return left <= right;
            default: throw new IllegalArgumentException("Invalid operator: " + op);
        }
    }

    private boolean compare(int left, int right, String op) {
        return compare((double) left, (double) right, op);
    }

    private boolean compare(boolean left, boolean right, String op) {
        switch (op) {
            case "==": return left == right;
            case "!=": return left != right;
            default: throw new IllegalArgumentException("Invalid boolean operator: " + op);
        }
    }

    private boolean compare(String left, String right, String op) {
        switch (op) {
            case "==": return left.equals(right);
            case "!=": return !left.equals(right);
            default: throw new IllegalArgumentException("Invalid string operator: " + op);
        }
    }
}