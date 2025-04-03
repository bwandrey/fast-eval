package fasteval.context;

import java.util.HashMap;
import java.util.Map;

public class EvalContextImpl implements EvalContext {

    private final Map<String, Object> tokens = new HashMap<>();

    @Override
    public double getDouble(String tokenName) {
        Object val = tokens.get(tokenName);
        if (val instanceof Double) {
            return (Double) val;
        }
        throw new IllegalArgumentException("Token '" + tokenName + "' is missing or not a double");
    }

    @Override
    public int getInt(String tokenName) {
        Object val = tokens.get(tokenName);
        if (val instanceof Integer) {
            return (Integer) val;
        }
        throw new IllegalArgumentException("Token '" + tokenName + "' is missing or not an int");
    }

    @Override
    public boolean getBoolean(String tokenName) {
        Object val = tokens.get(tokenName);
        if (val instanceof Boolean) {
            return (Boolean) val;
        }
        throw new IllegalArgumentException("Token '" + tokenName + "' is missing or not a boolean");
    }

    @Override
    public String getString(String tokenName) {
        Object val = tokens.get(tokenName);
        if (val instanceof String) {
            return (String) val;
        }
        throw new IllegalArgumentException("Token '" + tokenName + "' is missing or not a string");
    }

    // Fluent setters
    public EvalContextImpl withDouble(String tokenName, double value) {
        tokens.put(tokenName, value);
        return this;
    }

    public EvalContextImpl withInt(String tokenName, int value) {
        tokens.put(tokenName, value);
        return this;
    }

    public EvalContextImpl withBoolean(String tokenName, boolean value) {
        tokens.put(tokenName, value);
        return this;
    }

    public EvalContextImpl withString(String tokenName, String value) {
        tokens.put(tokenName, value);
        return this;
    }
}
