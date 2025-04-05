package fasteval.compilation;

import fasteval.model.RuleNode;

import java.util.HashSet;
import java.util.Set;

public class RuleNodeReferenceFinder {

    public static Set<String> findReferencedRuleNames(RuleNode node) {
        Set<String> result = new HashSet<>();
        walk(node, result);
        return result;
    }

    private static void walk(RuleNode node, Set<String> result) {
        if (node == null) return;

        if (node.getType() == RuleNode.Type.RULE_REFERENCE && node.getReferencedRule() != null) {
            result.add(node.getReferencedRule().getName());
        }

        walk(node.getLeft(), result);
        walk(node.getRight(), result);
    }
}
