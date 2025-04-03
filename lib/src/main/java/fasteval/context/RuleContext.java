package fasteval.context;

import fasteval.definitions.RuleDefinition;
import fasteval.definitions.TokenDefinition;
import fasteval.model.RuleNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;
import java.util.Set;
@Data
@AllArgsConstructor
public class RuleContext {

    private final Set<RuleDefinition> ruleDefinitionSet;
    private final Set<TokenDefinition> tokenDefinitionMap;
    private final Map<String, RuleNode> ruleNodeMap;


}
