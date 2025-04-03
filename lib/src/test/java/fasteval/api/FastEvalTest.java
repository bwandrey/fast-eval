package fasteval.api;

import fasteval.context.EvalContext;
import fasteval.context.EvalContextImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class FastEvalTest {

    private Path createTempRulesFile() throws IOException {
        String content = """
            tokens:
                stockPrice: double
                stockHalted: boolean

            rules:
                priceHigh: stockPrice > 100.0
                halted: stockHalted == true
            """;
        Path tempFile = Files.createTempFile("rules", ".txt");
        Files.writeString(tempFile, content);
        return tempFile;
    }

    @BeforeEach
    public void resetFastEval() throws Exception {
        // Reflection hack (since globalContext is private static)
        var field = FastEval.class.getDeclaredField("globalContext");
        field.setAccessible(true);
        field.set(null, null);
    }

    @Test
    public void testLoadFromFileAndCreateEngine() throws IOException {
        Path rulesFile = createTempRulesFile();

        // Should not throw
        FastEval.loadFromFile(rulesFile.toString());

        EvalContext ctx = new EvalContextImpl()
                .withDouble("stockPrice", 150.0)
                .withBoolean("stockHalted", false);

        FastEvalEngine engine = FastEval.withEvaluationContext(ctx);
        assertNotNull(engine);

        // Sanity check — evaluate a rule
        assertTrue(engine.evaluate("priceHigh"));
        assertFalse(engine.evaluate("halted"));
    }

    @Test
    public void testWithEvaluationContextWithoutLoadingFileThrows() {
        EvalContext ctx = new EvalContextImpl();

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            FastEval.withEvaluationContext(ctx);
        });

        assertEquals("FastEval.loadFromFile(...) must be called before using the evaluator.", ex.getMessage());
    }
}
