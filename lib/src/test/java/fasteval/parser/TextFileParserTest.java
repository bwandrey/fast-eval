package fasteval.parser;

import fasteval.TextFileParser;
import fasteval.definitions.RuleDefinition;
import fasteval.definitions.TokenDefinition;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TextFileParserTest {

    private String generateTempFile(String content) throws IOException {
        Path tempFile = Files.createTempFile("rules", ".txt");
        Files.writeString(tempFile, content);
        return tempFile.toAbsolutePath().toString();
    }

    @Test
    public void testParseTokens() throws IOException {
        String fileContent = """
                tokens:
                stockPrice: double
                stockHalted: boolean
                """;

        TextFileParser parser = new TextFileParser();
        parser.parseRulesFile(generateTempFile(fileContent));

        List<TokenDefinition> tokens = parser.getTokens();
        assertEquals(2, tokens.size());
        assertEquals("stockPrice", tokens.get(0).getName());
        assertEquals("double", tokens.get(0).getType());
        assertEquals("stockHalted", tokens.get(1).getName());
        assertEquals("boolean", tokens.get(1).getType());
    }

    @Test
    public void testParseRules() throws IOException {
        String fileContent = """
                rules:
                priceHigh: stockPrice > 100.0
                stockStopped: stockHalted == true
                """;

        TextFileParser parser = new TextFileParser();
        parser.parseRulesFile(generateTempFile(fileContent));

        List<RuleDefinition> rules = parser.getRules();
        assertEquals(2, rules.size());
        assertEquals("priceHigh", rules.get(0).getName());
        assertEquals("stockPrice > 100.0", rules.get(0).getExpression());
        assertEquals("stockStopped", rules.get(1).getName());
        assertEquals("stockHalted == true", rules.get(1).getExpression());
    }

    @Test
    public void testParseMixedTokensAndRules() throws IOException {
        String fileContent = """
                tokens:
                stockPrice: double
                stockHalted: boolean

                rules:
                priceHigh: stockPrice > 100.0
                stockStopped: stockHalted == true
                """;

        TextFileParser parser = new TextFileParser();
        parser.parseRulesFile(generateTempFile(fileContent));

        assertEquals(2, parser.getTokens().size());
        assertEquals(2, parser.getRules().size());
    }

    @Test
    public void testIgnoresEmptyLinesAndWhitespace() throws IOException {
        String fileContent = """
                
                tokens:
                stockPrice: double

                rules:

                priceHigh: stockPrice > 100.0
                
                """;

        TextFileParser parser = new TextFileParser();
        parser.parseRulesFile(generateTempFile(fileContent));

        assertEquals(1, parser.getTokens().size());
        assertEquals(1, parser.getRules().size());
    }

    @Test
    public void testUnimplementedGroupsSection() throws IOException {
        String fileContent = """
                groups:
                someGroup: ruleA AND ruleB
                """;

        TextFileParser parser = new TextFileParser();
        // Should not throw or fail
        parser.parseRulesFile(generateTempFile(fileContent));

        assertEquals(0, parser.getTokens().size());
        assertEquals(0, parser.getRules().size());
    }
}
