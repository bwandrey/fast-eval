package fasteval.context;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EvalContextImplTest {

    @Test
    public void testWithAndGetDouble() {
        EvalContext ctx = new EvalContextImpl().withDouble("price", 99.9);
        assertEquals(99.9, ctx.getDouble("price"));
    }

    @Test
    public void testWithAndGetInt() {
        EvalContext ctx = new EvalContextImpl().withInt("count", 42);
        assertEquals(42, ctx.getInt("count"));
    }

    @Test
    public void testWithAndGetBoolean() {
        EvalContext ctx = new EvalContextImpl().withBoolean("active", true);
        assertTrue(ctx.getBoolean("active"));
    }

    @Test
    public void testWithAndGetString() {
        EvalContext ctx = new EvalContextImpl().withString("status", "OK");
        assertEquals("OK", ctx.getString("status"));
    }

    @Test
    public void testMissingDoubleThrows() {
        EvalContext ctx = new EvalContextImpl();
        Exception e = assertThrows(IllegalArgumentException.class, () -> ctx.getDouble("missing"));
        assertTrue(e.getMessage().contains("Token 'missing' is missing or not a double"));
    }

    @Test
    public void testMissingIntThrows() {
        EvalContext ctx = new EvalContextImpl();
        Exception e = assertThrows(IllegalArgumentException.class, () -> ctx.getInt("missing"));
        assertTrue(e.getMessage().contains("Token 'missing' is missing or not an int"));
    }

    @Test
    public void testMissingBooleanThrows() {
        EvalContext ctx = new EvalContextImpl();
        Exception e = assertThrows(IllegalArgumentException.class, () -> ctx.getBoolean("missing"));
        assertTrue(e.getMessage().contains("Token 'missing' is missing or not a boolean"));
    }

    @Test
    public void testMissingStringThrows() {
        EvalContext ctx = new EvalContextImpl();
        Exception e = assertThrows(IllegalArgumentException.class, () -> ctx.getString("missing"));
        assertTrue(e.getMessage().contains("Token 'missing' is missing or not a string"));
    }

    @Test
    public void testWrongTypeThrows() {
        EvalContextImpl ctx = new EvalContextImpl().withInt("number", 10);

        assertThrows(IllegalArgumentException.class, () -> ctx.getDouble("number"));
        assertThrows(IllegalArgumentException.class, () -> ctx.getBoolean("number"));
        assertThrows(IllegalArgumentException.class, () -> ctx.getString("number"));
    }
}
