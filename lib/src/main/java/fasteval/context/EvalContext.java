package fasteval.context;

public interface EvalContext {

    double getDouble(String tokenName);

    int getInt(String tokenName);

    boolean getBoolean(String tokenName);

    String getString(String tokenName);

    EvalContext withDouble(String tokenName, double value);

    EvalContext withInt(String tokenName, int value);

    EvalContext withBoolean(String tokenName, boolean value);

    EvalContext withString(String tokenName, String value);
}