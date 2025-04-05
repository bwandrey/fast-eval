package fasteval.compilation;

import fasteval.compilationv2.InMemoryJavaCompiler;
import fasteval.compilationv2.RuleNodeJavaGenerator;
import fasteval.model.CompiledRule;
import fasteval.model.RuleNode;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.stream.Collectors;

public class RuleNodeCompiler {

    private final boolean useStringCompiler;

    public RuleNodeCompiler() {
        this(false);
    }

    public RuleNodeCompiler(boolean useStringCompiler) {
        this.useStringCompiler = useStringCompiler;
    }

    public CompiledRule compile(String ruleName, RuleNode node, Map<String, CompiledRule> compiledRuleMap) {
        return useStringCompiler
                ? compileWithStringCompiler(ruleName, node, compiledRuleMap)
                : compileWithByteBuddy(ruleName, node, compiledRuleMap);
    }

    private CompiledRule compileWithStringCompiler(String ruleName, RuleNode node, Map<String, CompiledRule> compiledRuleMap) {
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
            throw new RuntimeException("Failed to compile rule via string compiler: " + ruleName, e);
        }
    }

    private CompiledRule compileWithByteBuddy(String ruleName, RuleNode node, Map<String, CompiledRule> compiledRuleMap) {
        throw new UnsupportedOperationException("ByteBuddy backend not implemented in this version.");
        // If you still want to support ByteBuddy:
        // - restore your previous working bytecode generation logic here
        // - use `new ByteBuddy()` and inject fields + evaluate method + constructor
    }
}
