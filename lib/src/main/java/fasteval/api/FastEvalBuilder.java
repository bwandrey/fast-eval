package fasteval.api;

import fasteval.compilation.RuleNodeCompiler;
import fasteval.compilation.RuleNodeReferenceFinder;
import fasteval.context.CompiledRuleContext;
import fasteval.context.ObjectRuleContext;
import fasteval.definitions.RuleDefinition;
import fasteval.definitions.TokenDefinition;
import fasteval.model.CompiledRule;
import fasteval.model.RuleNode;
import fasteval.parser.ExpressionParser;

import java.util.*;
import java.util.stream.Collectors;

public class FastEvalBuilder {
    private final ExpressionParser parser;
    private final List<RuleDefinition> rules;
    private final List<TokenDefinition> tokens;
    private final Map<String, List<String>> groups;

    private boolean useStringCompilation = false;

    public FastEvalBuilder(ExpressionParser parser,
                           List<RuleDefinition> rules,
                           List<TokenDefinition> tokens,
                           Map<String, List<String>> groups) {
        this.parser = parser;
        this.rules = rules;
        this.tokens = tokens;
        this.groups = groups;
    }

    public void usingInterpretation() {
        Map<String, RuleNode> ruleNodeMap = parser.parseAllToMap();
        Map<String, String> tokenTypeMap = tokens.stream()
                .collect(Collectors.toMap(TokenDefinition::getName, TokenDefinition::getType));

        ObjectRuleContext context = new ObjectRuleContext(ruleNodeMap, groups, tokens);
        FastEval.setGlobalContext(context);
    }

    public void usingCompilation() {
        compileRules(false);
    }

    public void usingStringCompilation() {
        compileRules(true);
    }

    private void compileRules(boolean stringMode) {
        Map<String, RuleNode> ruleNodeMap = parser.parseAllToMap();
        Map<String, CompiledRule> compiledMap = new HashMap<>();
        RuleNodeCompiler compiler = new RuleNodeCompiler(stringMode);

        Set<String> compiled = new HashSet<>();

        // Collect token types map
        Map<String, String> tokenTypeMap = tokens.stream()
                .collect(Collectors.toMap(TokenDefinition::getName, TokenDefinition::getType));

        while (compiled.size() < ruleNodeMap.size()) {
            boolean progress = false;
            for (Map.Entry<String, RuleNode> entry : ruleNodeMap.entrySet()) {
                String ruleName = entry.getKey();
                if (compiled.contains(ruleName)) continue;

                Set<String> deps = RuleNodeReferenceFinder.findReferencedRuleNames(entry.getValue());
                if (compiled.containsAll(deps)) {
                    CompiledRule compiledRule = compiler.compile(ruleName, entry.getValue(), compiledMap);
                    compiledMap.put(ruleName, compiledRule);
                    compiled.add(ruleName);
                    progress = true;
                }
            }
            if (!progress) throw new IllegalStateException("Cyclic or unresolved dependency in rules");
        }

        CompiledRuleContext context = new CompiledRuleContext(compiledMap, groups);
        FastEval.setGlobalContext(context);
    }
}
