package fasteval.parser;


import fasteval.definitions.RuleDefinition;
import fasteval.definitions.TokenDefinition;
import fasteval.model.RuleNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class ExpressionParserTest {

    private ExpressionParser parser;

    @BeforeEach
    public void setup() {
        Set<RuleDefinition> rules = Set.of(
                new RuleDefinition("priceHigh", "stockPrice > 100.0"),
                new RuleDefinition("halted", "stockHalted == true")
        );
        List<TokenDefinition> tokens = List.of(
                new TokenDefinition("stockPrice", "double"),
                new TokenDefinition("stockHalted", "boolean")
        );
        parser = new ExpressionParser(rules, tokens);
    }

    @Test
    public void testParseComparison() {
        RuleNode node = parser.parse("stockPrice > 200.0");

        assertEquals(RuleNode.Type.COMPARISON, node.getType());
        assertEquals("stockPrice", node.getTokenName());
        assertEquals(">", node.getOperator());
        assertEquals("200.0", node.getValue());
    }

    @Test
    public void testParseRuleReference() {
        RuleNode node = parser.parse("priceHigh");

        assertEquals(RuleNode.Type.RULE_REFERENCE, node.getType());
        assertEquals("priceHigh", node.getReferencedRule().getName());
    }

    @Test
    public void testParseBooleanTokenAsImplicitComparison() {
        RuleNode node = parser.parse("stockHalted");

        assertEquals(RuleNode.Type.COMPARISON, node.getType());
        assertEquals("stockHalted", node.getTokenName());
        assertEquals("==", node.getOperator());
        assertEquals("true", node.getValue());
    }

    @Test
    public void testParseLogicalAnd() {
        RuleNode node = parser.parse("priceHigh AND stockHalted");

        assertEquals(RuleNode.Type.AND, node.getType());
        assertEquals(RuleNode.Type.RULE_REFERENCE, node.getLeft().getType());
        assertEquals(RuleNode.Type.COMPARISON, node.getRight().getType());
    }

    @Test
    public void testParseLogicalOrAndParentheses() {
        RuleNode node = parser.parse("(priceHigh AND stockHalted) OR stockPrice > 250");

        assertEquals(RuleNode.Type.OR, node.getType());
        assertEquals(RuleNode.Type.AND, node.getLeft().getType());
        assertEquals(RuleNode.Type.COMPARISON, node.getRight().getType());
    }

    @Test
    public void testParseNotOperator() {
        RuleNode node = parser.parse("NOT stockHalted");

        assertEquals(RuleNode.Type.NOT, node.getType());
        assertEquals("stockHalted", node.getLeft().getTokenName());
    }

    @Test
    public void testUnexpectedTokenThrows() {
        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            parser.parse("priceHigh && stockHalted"); // '&&' is invalid
        });

        assertTrue(e.getMessage().contains("Unexpected token"));
    }

    @Test
    public void testUnexpectedEndOfExpression() {
        Exception e = assertThrows(RuntimeException.class, () -> {
            parser.parse("stockPrice >");
        });

        assertTrue(e.getMessage().contains("Unexpected end of expression"));
    }

    @Test
    public void testParseAllToMap() {
        Map<String, RuleNode> map = parser.parseAllToMap();

        assertEquals(2, map.size());
        assertTrue(map.containsKey("priceHigh"));
        assertTrue(map.containsKey("halted"));

        RuleNode halted = map.get("halted");
        assertEquals(RuleNode.Type.COMPARISON, halted.getType());
        assertEquals("stockHalted", halted.getTokenName());
    }

    @Test
    public void testUnknownStandaloneTokenThrows() {
        parser = new ExpressionParser(Set.of(), List.of(
                new TokenDefinition("stockPrice", "double") // not boolean
        ));

        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            parser.parse("stockPrice"); // stockPrice is not boolean
        });

        assertTrue(e.getMessage().contains("non-boolean token"));
    }
}
