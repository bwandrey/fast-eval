package fasteval.context;

import fasteval.model.CompiledRule;
import fasteval.model.RuleRepresentation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompiledRuleContext implements RuleContextInterface {
    private final Map<String, CompiledRule> compiledRules;
    private final Map<String, List<String>> ruleGroups;

    public CompiledRuleContext(Map<String, CompiledRule> compiledRules,
                               Map<String, List<String>> ruleGroups) {
        this.compiledRules = compiledRules;
        this.ruleGroups = ruleGroups;
    }

    @Override
    public Map<String, RuleRepresentation> getRules() {
        return new HashMap<>(compiledRules); // CompiledRule extends RuleRepresentation
    }

    @Override
    public Map<String, List<String>> getRuleGroups() {
        return ruleGroups;
    }

    @Override
    public Map<String, String> getTokenTypeMap() {
        // Return an empty map as compiled rules don't need token types
        return new HashMap<>();
    }
}
