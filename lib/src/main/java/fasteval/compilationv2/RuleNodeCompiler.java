package fasteval.compilationv2;

import fasteval.compilation.RuleNodeReferenceFinder;
import fasteval.model.CompiledRule;
import fasteval.model.RuleNode;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.stream.Collectors;

public class RuleNodeCompiler {

    public CompiledRule compile(String ruleName, RuleNode node, Map<String, CompiledRule> compiledRuleMap) {
        try {
            String className = "Rule_" + ruleName;
            String fullClassName = "fasteval.compiled." + className;

            // 1. Generate Java source code for the rule
            RuleNodeJavaGenerator generator = new RuleNodeJavaGenerator(className, node);
            String javaCode = generator.generate();

            // 2. Compile the source code and load the class
            Class<?> compiledClass = InMemoryJavaCompiler.compile(fullClassName, javaCode);

            // 3. Find all referenced rules
            Set<String> references = RuleNodeReferenceFinder.findReferencedRuleNames(node);

            // 4. Get constructor and prepare parameters
            Constructor<?> constructor = compiledClass.getDeclaredConstructors()[0];
            List<CompiledRule> params = references.stream()
                    .map(compiledRuleMap::get)
                    .collect(Collectors.toList());

            // 5. Instantiate and return
            return (CompiledRule) constructor.newInstance(params.toArray());

        } catch (Exception e) {
            throw new RuntimeException("Failed to compile rule: " + ruleName, e);
        }
    }
}
