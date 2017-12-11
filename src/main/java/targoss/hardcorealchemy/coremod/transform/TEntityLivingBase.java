package targoss.hardcorealchemy.coremod.transform;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import targoss.hardcorealchemy.coremod.MethodPatcher;
import targoss.hardcorealchemy.coremod.ObfuscatedName;

public class TEntityLivingBase extends MethodPatcher {
    private static final String ENTITY_LIVING_BASE = "net.minecraft.entity.EntityLivingBase";
	private static final ObfuscatedName ENTITY_DAMAGE = new ObfuscatedName("attackEntityFrom", "func_70097_a");
	
	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if (transformedName.equals(ENTITY_LIVING_BASE)) {
		    return transformClass(transformedName, basicClass, 0);
		}
		return basicClass;
	}

    @Override
    public void transformMethod(MethodNode method) {
        if (method.name.equals(ENTITY_DAMAGE.get())) {
            InsnList instructions = method.instructions;
            
            InsnList eventHook = new InsnList();
            eventHook.add(new VarInsnNode(Opcodes.ALOAD, 0));
            eventHook.add(new VarInsnNode(Opcodes.ALOAD, 1));
            eventHook.add(new VarInsnNode(Opcodes.FLOAD, 2));
            eventHook.add(new MethodInsnNode(
                    Opcodes.INVOKESTATIC, "targoss/hardcorealchemy/event/EventLivingAttack",
                    "onLivingAttack", "(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/util/DamageSource;F)F", false
                    ));
            eventHook.add(new VarInsnNode(Opcodes.FSTORE, 2));
            
            instructions.insert(eventHook);
        }
    }
	
}
