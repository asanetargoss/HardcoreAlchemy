package targoss.hardcorealchemy.tweaks.coremod.transform;

import java.util.ListIterator;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import targoss.hardcorealchemy.coremod.MethodPatcher;
import targoss.hardcorealchemy.coremod.ObfuscatedName;

public class TEntityArrow extends MethodPatcher {
    protected static final String ENTITY_ARROW = "net.minecraft.entity.projectile.EntityArrow";
    protected static final ObfuscatedName ON_HIT = new ObfuscatedName("func_184549_a" /*onHit*/);
    protected static final ObfuscatedName CAUSE_ARROW_DAMAGE = new ObfuscatedName("func_76353_a" /*causeArrowDamage*/);

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.equals(ENTITY_ARROW)) {
            return transformClass(transformedName, basicClass, 0);
        }
        return basicClass;
    }

    @Override
    public void transformMethod(MethodNode method) {
        if (method.name.equals(ON_HIT.get())) {
            InsnList insns = method.instructions;
            ListIterator<AbstractInsnNode> iter = insns.iterator();
            while (iter.hasNext()) {
                AbstractInsnNode insn = iter.next();
                if (insn.getOpcode() == Opcodes.INVOKESTATIC && ((MethodInsnNode)insn).name.equals(CAUSE_ARROW_DAMAGE.get())) {
                    InsnList patch = new InsnList();
                    patch.add(new InsnNode(Opcodes.DUP)); // DamageSource (arrow)
                    patch.add(new VarInsnNode(Opcodes.ALOAD, 1)); // RayTraceResult
                    patch.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                            "targoss/hardcorealchemy/tweaks/event/EventArrowHit",
                            "onArrowHit",
                            "(Lnet/minecraft/util/DamageSource;Lnet/minecraft/util/math/RayTraceResult;)V",
                            false));
                    insns.insert(insn, patch);
                }
            }
        }
    }

}
