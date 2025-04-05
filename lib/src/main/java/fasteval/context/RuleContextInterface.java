package fasteval.context;

import fasteval.model.RuleRepresentation;

import java.util.List;
import java.util.Map;

public interface RuleContextInterface {
    Map<String, RuleRepresentation> getRules();
    Map<String, List<String>> getRuleGroups();

    // Add a method to fetch the token type map
    Map<String, String> getTokenTypeMap();
}