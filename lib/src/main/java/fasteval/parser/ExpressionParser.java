package fasteval.parser;

import fasteval.context.RuleContext;
import fasteval.definitions.RuleDefinition;
import fasteval.definitions.TokenDefinition;
import fasteval.model.RuleNode;
import lombok.ToString;

import java.util.*;
import java.util.stream.Collectors;

@ToString
public class ExpressionParser {
    private final Set<String> ruleDefinitionsNamesSet;
    private final Set<RuleDefinition> ruleDefinitionSet;
    private final Map<String, TokenDefinition> tokenNameTokenDefinitionMap;
    private List<String> tokens;
    private int pos;

    public ExpressionParser(Set<RuleDefinition> ruleDefinitionSet, List<TokenDefinition> tokensList) {
        this.ruleDefinitionSet = ruleDefinitionSet;
        this.ruleDefinitionsNamesSet = ruleDefinitionSet.stream().map(RuleDefinition::getName).collect(Collectors.toSet());

        this.tokenNameTokenDefinitionMap = new HashMap<>();
        for (TokenDefinition td : tokensList) {
            tokenNameTokenDefinitionMap.put(td.getName(), td);
        }
    }

    public RuleNode parse(String expr) {
        tokens = tokenize(expr);
        pos = 0;

        RuleNode node = parseExpression();

        // After parsing a complete expression, ensure we consumed everything
        if (pos < tokens.size()) {
            throw new IllegalArgumentException("Unexpected token: '" + tokens.get(pos) + "'");
        }

        return node;
    }

    public RuleContext generateRuleContext() {
        return new RuleContext(ruleDefinitionSet, new HashSet<>(tokenNameTokenDefinitionMap.values()), this.parseAllToMap());
    }

    private List<String> tokenize(String expr) {
        return Arrays.asList(expr.replace("(", " ( ")
                .replace(")", " ) ")
                .trim().split("\\s+"));
    }

    private RuleNode parseExpression() {
        RuleNode node = parseTerm();
        while (peek("AND", "OR", "XOR")) {
            String operator = consume();
            RuleNode right = parseTerm();
            node = RuleNode.logical(RuleNode.Type.valueOf(operator), node, right);
        }
        return node;
    }

    private RuleNode parseTerm() {
        if (peek("NOT")) {
            consume("NOT");
            RuleNode inner = parseTerm(); // NOT is right-associative
            return RuleNode.not(inner);
        } else if (peek("(")) {
            consume("(");
            RuleNode node = parseExpression();
            consume(")");
            return node;
        } else {
            return parseAtom();
        }
    }

    private RuleNode parseAtom() {
        String token = consume();

        if (peek(">", "<", "==", "!=", ">=", "<=")) {
            String operator = consume();
            String value = consume();
            return RuleNode.comparison(token, operator, value);

        } else if (ruleDefinitionsNamesSet.contains(token)) {
            RuleDefinition matchedRule = ruleDefinitionSet.stream()
                    .filter(r -> r.getName().equals(token))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Rule definition not found for: " + token));
            return RuleNode.ruleRef(matchedRule);

        } else if (tokenNameTokenDefinitionMap.containsKey(token)) {
            TokenDefinition def = tokenNameTokenDefinitionMap.get(token);
            if ("boolean".equalsIgnoreCase(def.getType())) {
                // treat the presence of a boolean token as: token == true
                return RuleNode.comparison(token, "==", "true");
            } else {
                throw new IllegalArgumentException("Cannot use non-boolean token '" + token + "' as standalone rule");
            }
        }

        throw new IllegalArgumentException("Unknown token or rule reference: " + token);
    }

    private boolean peek(String... options) {
        if (pos >= tokens.size()) return false;
        for (String option : options) {
            if (tokens.get(pos).equals(option)) return true;
        }
        return false;
    }

    private String consume() {
        if (pos >= tokens.size()) {
            throw new RuntimeException("Unexpected end of expression.");
        }
        return tokens.get(pos++);
    }

    private void consume(String expected) {
        String actual = consume();
        if (!actual.equals(expected)) {
            throw new IllegalArgumentException("Expected '" + expected + "', found '" + actual + "'");
        }
    }

    public Map<String, RuleNode> parseAllToMap() {
        Map<String, RuleNode> result = new LinkedHashMap<>();

        for (RuleDefinition rule : ruleDefinitionSet) {
            RuleNode ast = parse(rule.getExpression());
            result.put(rule.getName(), ast);
        }

        return result;
    }
}
