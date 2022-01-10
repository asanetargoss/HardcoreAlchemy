package targoss.hardcorealchemy.tweaks.coremod.transform;

import java.util.ListIterator;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import targoss.hardcorealchemy.coremod.MethodPatcher;
import targoss.hardcorealchemy.coremod.ObfuscatedName;

public class TEntityAIAttackMelee extends MethodPatcher {
    protected static final String ENTITY_AI_ATTACK_MELEE = "net.minecraft.entity.ai.EntityAIAttackMelee";
    protected static final ObfuscatedName CHECK_AND_PERFORM_ATTACK = new ObfuscatedName("func_190102_a" /*checkAndPeformAttack*/);
    protected static final ObfuscatedName ATTACK_ENTITY_AS_MOB = new ObfuscatedName("func_70652_k" /*attackEntityAsMob*/);
    protected static final ObfuscatedName ATTACKER = new ObfuscatedName("field_75441_b" /*EntityAIAttackMelee::attacker*/);

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.equals(ENTITY_AI_ATTACK_MELEE)) {
            return transformClass(transformedName, basicClass, 0);
        }
        return basicClass;
    }

    @Override
    public void transformMethod(MethodNode method) {
        if (method.name.equals(CHECK_AND_PERFORM_ATTACK.get())) {
            InsnList insns = method.instructions;
            ListIterator<AbstractInsnNode> iter = insns.iterator();
            while (iter.hasNext()) {
                AbstractInsnNode insn = iter.next();
                if (insn.getOpcode() == Opcodes.INVOKEVIRTUAL && ((MethodInsnNode)insn).name.equals(ATTACK_ENTITY_AS_MOB.get())) {
                    InsnList patch = new InsnList();
                    // EventMeleeAttack.onMeleeAttack(the attack target, this.attacker)
                    patch.add(new VarInsnNode(Opcodes.ALOAD, 1)); // the attack target
                    patch.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    patch.add(new FieldInsnNode(Opcodes.GETFIELD,
                            ENTITY_AI_ATTACK_MELEE.replace(".", "/"),
                            ATTACKER.get(),
                            "Lnet/minecraft/entity/EntityCreature;")); // this.attacker
                    patch.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                            "targoss/hardcorealchemy/tweaks/event/EventMeleeAttack",
                            "onMeleeAttack",
                            "(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/entity/EntityCreature;)V",
                            false));
                    insns.insert(insn, patch);
                }
            }
        }
    }

}
