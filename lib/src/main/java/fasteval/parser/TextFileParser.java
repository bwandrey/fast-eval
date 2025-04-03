package fasteval;

import fasteval.definitions.RuleDefinition;
import fasteval.definitions.TokenDefinition;
import lombok.Getter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Getter
public class TextFileParser
{
    List<TokenDefinition> tokens = new ArrayList<>();
    List<RuleDefinition> rules = new ArrayList<>();

    public void parseRulesFile(String filePath) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        boolean parsingTokens = false;
        boolean parsingRules = false;
        boolean parsingGroups = false;
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            if (line.startsWith("tokens:")) {
                parsingTokens = true; parsingRules = false;parsingGroups = false; continue;
            } else if (line.startsWith("rules:")) {
                parsingRules = true; parsingTokens = false;parsingGroups = false; continue;
            }else if (line.startsWith("groups:")) {
                parsingGroups = true; parsingRules = false; parsingTokens = false; continue;
            }

            if (parsingTokens) {
                // Example line: stockPrice: double
                String[] parts = line.split(":");
                tokens.add(new TokenDefinition(parts[0].trim(), parts[1].trim()));
            } else if (parsingRules) {
                // Example line: priceHigh: stockPrice > 100.0
                String[] parts = line.split(":", 2);
                rules.add(new RuleDefinition(parts[0].trim(), parts[1].trim()));
            }else if( parsingGroups) {
                System.out.println("not yet implemented");
            }
        }
    }
}
