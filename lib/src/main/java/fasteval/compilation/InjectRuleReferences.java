package fasteval.compilation;

import fasteval.model.CompiledRule;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.bytecode.ByteCodeAppender;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.implementation.bytecode.member.FieldAccess;
import net.bytebuddy.implementation.bytecode.member.MethodVariableAccess;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class InjectRuleReferences implements Implementation {

    private final List<String> referencedRuleNames;

    public InjectRuleReferences(Set<String> referencedRuleNames) {
        this.referencedRuleNames = new ArrayList<>(referencedRuleNames);
    }

    @Override
    public ByteCodeAppender appender(Target implementationTarget) {
        return (MethodVisitor mv, Context context, net.bytebuddy.description.method.MethodDescription method) -> {
            List<StackManipulation> manipulations = new ArrayList<>();

            for (int i = 0; i < referencedRuleNames.size(); i++) {
                String refName = referencedRuleNames.get(i);
                FieldDescription field = implementationTarget.getInstrumentedType()
                        .getDeclaredFields()
                        .filter(f -> f.getName().equals("ref_" + refName))
                        .getOnly();

                manipulations.add(new StackManipulation.Compound(
                        MethodVariableAccess.loadThis(),
                        MethodVariableAccess.REFERENCE.loadFrom(i + 1),
                        FieldAccess.forField(field).write()
                ));
            }

            StackManipulation compound = new StackManipulation.Compound(manipulations);
            StackManipulation.Size size = compound.apply(mv, context);

            mv.visitInsn(Opcodes.RETURN);

            return new ByteCodeAppender.Size(size.getMaximalSize(), method.getStackSize());
        };
    }

    @Override
    public net.bytebuddy.dynamic.scaffold.InstrumentedType prepare(
            net.bytebuddy.dynamic.scaffold.InstrumentedType instrumentedType) {
        return instrumentedType;
    }
}
