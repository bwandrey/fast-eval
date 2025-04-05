package fasteval.context;

import fasteval.definitions.TokenDefinition;
import fasteval.model.InterpretedRule;
import fasteval.model.RuleNode;
import fasteval.model.RuleRepresentation;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ObjectRuleContext implements RuleContextInterface {
    @Getter
    private final Map<String, RuleNode> ruleNodeMap;
    private final Map<String, List<String>> ruleGroups;
    private final Map<String, String> tokenTypeMap;

    public ObjectRuleContext(Map<String, RuleNode> ruleNodeMap,
                             Map<String, List<String>> ruleGroups,
                             List<TokenDefinition> tokens) {
        this.ruleNodeMap = ruleNodeMap;
        this.ruleGroups = ruleGroups;
        this.tokenTypeMap = tokens.stream()
                .collect(Collectors.toMap(TokenDefinition::getName, TokenDefinition::getType));
    }

    @Override
    public Map<String, RuleRepresentation> getRules() {
        return ruleNodeMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> new InterpretedRule(e.getValue(), ruleNodeMap, tokenTypeMap)
                ));
    }

    @Override
    public Map<String, List<String>> getRuleGroups() {
        return ruleGroups;
    }

    // Implement the getTokenTypeMap method
    @Override
    public Map<String, String> getTokenTypeMap() {
        return tokenTypeMap;
    }

}
