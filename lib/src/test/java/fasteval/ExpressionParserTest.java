package fasteval;

import fasteval.definitions.RuleDefinition;
import fasteval.definitions.TokenDefinition;
import fasteval.model.RuleNode;
import fasteval.parser.ExpressionParser;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class ExpressionParserTest {

    private ExpressionParser parser;

    @BeforeEach
    public void setup() {
        Set<RuleDefinition> ruleDefinitionSet = new HashSet<>(Arrays.asList(
                new RuleDefinition("priceHigh", "stockPrice > 25.0"),
                new RuleDefinition("stockStopped", "stockHalted == true")
        ));
        List<TokenDefinition> tokens = Arrays.asList(
                new TokenDefinition("stockPrice", "double"),
                new TokenDefinition("stockHalted", "boolean")
        );
        parser = new ExpressionParser(ruleDefinitionSet, tokens);
    }

    @Test
    public void testParseComparisonExpression() {
        RuleNode node = parser.parse("stockPrice > 100.0");
        assertEquals(RuleNode.Type.COMPARISON, node.getType());
        assertEquals("stockPrice", node.getTokenName());
        assertEquals(">", node.getOperator());
        assertEquals("100.0", node.getValue());
    }

    @Test
    public void testParseSimpleRuleReference() {
        RuleNode node = parser.parse("priceHigh");
        assertEquals(RuleNode.Type.RULE_REFERENCE, node.getType());
        assertEquals("priceHigh", node.getReferencedRule());
    }

    @Test
    public void testParseLogicalAndExpression() {
        RuleNode node = parser.parse("priceHigh AND stockHalted");
        assertEquals(RuleNode.Type.AND, node.getType());
        assertNotNull(node.getLeft());
        assertNotNull(node.getRight());
        assertEquals(RuleNode.Type.RULE_REFERENCE, node.getLeft().getType());
        assertEquals("priceHigh", node.getLeft().getReferencedRule());
        assertEquals(RuleNode.Type.RULE_REFERENCE, node.getRight().getType());
        assertEquals("stockHalted", node.getRight().getReferencedRule());

    }

    @Test
    public void testParseExpressionWithParentheses() {
        RuleNode node = parser.parse("(stockPrice > 100.0) AND (stockHalted == true)");
        assertEquals(RuleNode.Type.AND, node.getType());
        assertEquals(RuleNode.Type.COMPARISON, node.getLeft().getType());
        assertEquals(RuleNode.Type.COMPARISON, node.getRight().getType());
    }

    @Test
    public void testParseNestedLogicalExpression() {
        RuleNode node = parser.parse("priceHigh AND stockHalted OR emergencyStop");
        assertEquals(RuleNode.Type.OR, node.getType());
        assertEquals(RuleNode.Type.AND, node.getLeft().getType());
        assertEquals(RuleNode.Type.RULE_REFERENCE, node.getRight().getType());
        assertEquals("emergencyStop", node.getRight().getReferencedRule());
    }
}
