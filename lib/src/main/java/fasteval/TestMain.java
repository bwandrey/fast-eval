package fasteval;

import fasteval.api.FastEval;
import fasteval.context.EvalContext;
import fasteval.context.EvalContextImpl;

import java.io.IOException;

public class TestMain {



    public static void main(String[] args) throws IOException {
           FastEval.loadFromFile("rules.txt").usingStringCompilation();
           EvalContext ctx = new EvalContextImpl()
                    .withDouble("stockPrice", 150.0)
                    .withBoolean("stockHalted", true);



           System.out.println(FastEval.withEvaluationContext(ctx).evaluateGroup("groupa"));
           System.out.println(FastEval.withEvaluationContext(ctx).evaluateGroup("groupb"));

    }
}
