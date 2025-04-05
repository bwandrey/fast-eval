package fasteval.compilation;

import fasteval.context.EvalContext;
import fasteval.model.CompiledRule;
import fasteval.model.RuleNode;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.scaffold.InstrumentedType;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.bytecode.ByteCodeAppender;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.implementation.bytecode.constant.TextConstant;
import net.bytebuddy.implementation.bytecode.member.FieldAccess;
import net.bytebuddy.implementation.bytecode.member.MethodInvocation;
import net.bytebuddy.implementation.bytecode.member.MethodVariableAccess;
import net.bytebuddy.jar.asm.Label;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Method;

public class RuleEvaluationImplementation implements Implementation {

    private final RuleNode node;

    public RuleEvaluationImplementation(RuleNode node) {
        this.node = node;
    }

    @Override
    public ByteCodeAppender appender(Target implementationTarget) {
        return (mv, context, method) -> {
            StackManipulation stackManipulation = compileNode(node, implementationTarget);
            StackManipulation.Size size = stackManipulation.apply(mv, context);
            mv.visitInsn(Opcodes.IRETURN);
            return new ByteCodeAppender.Size(size.getMaximalSize(), method.getStackSize());
        };
    }


    private StackManipulation compileNode(RuleNode node, Target target) {
        switch (node.getType()) {
            case COMPARISON: return compileComparison(node);
            case AND: return combineLogical(node, Opcodes.IAND, target);
            case OR: return combineLogical(node, Opcodes.IOR, target);
            case XOR: return combineLogical(node, Opcodes.IXOR, target);
            case NOT: return negate(compileNode(node.getLeft(), target));
            case RULE_REFERENCE: return compileRuleReference(node, target);
            default: throw new IllegalStateException("Unsupported node type: " + node.getType());
        }
    }

    private StackManipulation compileComparison(RuleNode node) {
        String tokenName = node.getTokenName();
        String valueStr = node.getValue();
        String operator = node.getOperator();

        try {
            // Try parse as int
            int intVal = Integer.parseInt(valueStr);
            Method getIntMethod = EvalContext.class.getMethod("getInt", String.class);
            return compileIntComparison(getIntMethod, tokenName, intVal, operator);
        } catch (NumberFormatException e) {
            try {
                // Try parse as boolean
                boolean boolVal = Boolean.parseBoolean(valueStr);
                Method getBoolMethod = EvalContext.class.getMethod("getBoolean", String.class);
                return compileBooleanComparison(getBoolMethod, tokenName, boolVal, operator);
            } catch (Exception inner) {
                throw new RuntimeException("Failed to compile boolean comparison for " + tokenName, inner);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to build comparison for: " + tokenName + " " + operator + " " + valueStr, e);
        }
    }

    @Override
    public InstrumentedType prepare(InstrumentedType instrumentedType) {
        return instrumentedType;
    }

    private StackManipulation compileIntComparison(Method getter, String token, int rightVal, String operator) {
        Label trueLabel = new Label();
        Label endLabel = new Label();

        return new StackManipulation.Compound(
                MethodVariableAccess.REFERENCE.loadFrom(1),
                new TextConstant(token),
                MethodInvocation.invoke(new MethodDescription.ForLoadedMethod(getter)),
                new StackManipulation() {
                    @Override public boolean isValid() { return true; }

                    @Override
                    public Size apply(MethodVisitor mv, Context context) {
                        mv.visitLdcInsn(rightVal);
                        switch (operator) {
                            case ">": mv.visitJumpInsn(Opcodes.IF_ICMPGT, trueLabel); break;
                            case "<": mv.visitJumpInsn(Opcodes.IF_ICMPLT, trueLabel); break;
                            case "==": mv.visitJumpInsn(Opcodes.IF_ICMPEQ, trueLabel); break;
                            case "!=": mv.visitJumpInsn(Opcodes.IF_ICMPNE, trueLabel); break;
                            case ">=": mv.visitJumpInsn(Opcodes.IF_ICMPGE, trueLabel); break;
                            case "<=": mv.visitJumpInsn(Opcodes.IF_ICMPLE, trueLabel); break;
                            default: throw new IllegalArgumentException("Unsupported operator: " + operator);
                        }
                        mv.visitInsn(Opcodes.ICONST_0);
                        mv.visitJumpInsn(Opcodes.GOTO, endLabel);
                        mv.visitLabel(trueLabel);
                        mv.visitInsn(Opcodes.ICONST_1);
                        mv.visitLabel(endLabel);
                        return new Size(2, 2);
                    }
                }
        );
    }

    private StackManipulation compileBooleanComparison(Method getter, String token, boolean expected, String operator) {
        Label trueLabel = new Label();
        Label endLabel = new Label();

        return new StackManipulation.Compound(
                MethodVariableAccess.REFERENCE.loadFrom(1),
                new TextConstant(token),
                MethodInvocation.invoke(new MethodDescription.ForLoadedMethod(getter)),
                new StackManipulation() {
                    @Override public boolean isValid() { return true; }

                    @Override
                    public Size apply(MethodVisitor mv, Context context) {
                        mv.visitInsn(expected ? Opcodes.ICONST_1 : Opcodes.ICONST_0);
                        switch (operator) {
                            case "==": mv.visitJumpInsn(Opcodes.IF_ICMPEQ, trueLabel); break;
                            case "!=": mv.visitJumpInsn(Opcodes.IF_ICMPNE, trueLabel); break;
                            default: throw new IllegalArgumentException("Unsupported boolean operator: " + operator);
                        }
                        mv.visitInsn(Opcodes.ICONST_0);
                        mv.visitJumpInsn(Opcodes.GOTO, endLabel);
                        mv.visitLabel(trueLabel);
                        mv.visitInsn(Opcodes.ICONST_1);
                        mv.visitLabel(endLabel);
                        return new Size(2, 2);
                    }
                }
        );
    }

    private StackManipulation combineLogical(RuleNode node, int opcode, Target target) {
        return new StackManipulation.Compound(
                compileNode(node.getLeft(), target),
                compileNode(node.getRight(), target),
                new StackManipulation() {
                    @Override public boolean isValid() { return true; }

                    @Override public Size apply(MethodVisitor mv, Context context) {
                        mv.visitInsn(opcode);
                        return new Size(1, 2);
                    }
                }
        );
    }

    private StackManipulation negate(StackManipulation inner) {
        Label trueLabel = new Label();
        Label endLabel = new Label();
        return new StackManipulation.Compound(
                inner,
                new StackManipulation() {
                    @Override public boolean isValid() { return true; }

                    @Override public Size apply(MethodVisitor mv, Context context) {
                        mv.visitJumpInsn(Opcodes.IFEQ, trueLabel);
                        mv.visitInsn(Opcodes.ICONST_0);
                        mv.visitJumpInsn(Opcodes.GOTO, endLabel);
                        mv.visitLabel(trueLabel);
                        mv.visitInsn(Opcodes.ICONST_1);
                        mv.visitLabel(endLabel);
                        return new Size(1, 1);
                    }
                }
        );
    }

    private StackManipulation compileRuleReference(RuleNode node, Target target) {
        String fieldName = "ref_" + node.getReferencedRule().getName();
        try {
            Method evaluateMethod = CompiledRule.class.getMethod("evaluate", EvalContext.class);
            return new StackManipulation.Compound(
                    MethodVariableAccess.loadThis(),
                    FieldAccess.forField(target.getInstrumentedType()
                            .getDeclaredFields()
                            .filter(ElementMatchers.named(fieldName))
                            .getOnly()).read(),
                    MethodVariableAccess.REFERENCE.loadFrom(1),
                    MethodInvocation.invoke(new MethodDescription.ForLoadedMethod(evaluateMethod))
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to compile rule reference: " + fieldName, e);
        }
    }
}
