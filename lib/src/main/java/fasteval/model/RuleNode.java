package fasteval.model;

import fasteval.definitions.RuleDefinition;
import lombok.Data;

@Data
public class RuleNode {
    public enum Type {
        AND, OR, XOR, NOT,
        COMPARISON, // e.g., stockPrice > 25.0
        RULE_REFERENCE
    }

    Type type;
    RuleNode left;
    RuleNode right;


    String tokenName;
    String operator;
    String value;

    // For RULE_REFERENCE
    RuleDefinition referencedRule;

    // Constructors (for clarity):
    public static RuleNode comparison(String token, String operator, String value) {
        RuleNode n = new RuleNode();
        n.type = Type.COMPARISON;
        n.tokenName = token;
        n.operator = operator;
        n.value = value;
        return n;
    }

    public static RuleNode logical(Type type, RuleNode left, RuleNode right) {
        RuleNode n = new RuleNode();
        n.type = type;
        n.left = left;
        n.right = right;
        return n;
    }

    public static RuleNode not(RuleNode inner) {
        RuleNode n = new RuleNode();
        n.type = Type.NOT;
        n.left = inner;
        return n;
    }

    public static RuleNode ruleRef(RuleDefinition rule) {
        RuleNode n = new RuleNode();
        n.type = Type.RULE_REFERENCE;
        n.referencedRule = rule;
        return n;
    }
}