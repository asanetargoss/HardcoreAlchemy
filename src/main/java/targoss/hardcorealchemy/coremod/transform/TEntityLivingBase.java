package targoss.hardcorealchemy.coremod.transform;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.minecraft.launchwrapper.IClassTransformer;
import targoss.hardcorealchemy.coremod.HardcoreAlchemyCoreMod;

public class TEntityLivingBase implements IClassTransformer {
	
    private String ENTITY_LIVING_BASE = "net.minecraft.entity.EntityLivingBase";
	private String[] ENTITY_DAMAGE = new String[]{"attackEntityFrom", "func_70097_a"};
	
	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if (transformedName.equals(ENTITY_LIVING_BASE)) {
		    return transformClass(transformedName, HardcoreAlchemyCoreMod.obfuscated, basicClass);
		}
		return basicClass;
	}
	
	private byte[] transformClass(String name, boolean obfuscated, byte[] basicClass) {
	    ClassReader reader = new ClassReader(basicClass);
	    ClassNode visitor = new ClassNode();
	    reader.accept(visitor, 0);
	    
	    for (MethodNode method : visitor.methods) {
	        if (method.name.equals(ENTITY_DAMAGE[0]) || method.name.equals(ENTITY_DAMAGE[1])) {
	            InsnList instructions = method.instructions;
	            
	            InsnList eventHook = new InsnList();
	            eventHook.add(new VarInsnNode(Opcodes.ALOAD, 0));
	            eventHook.add(new VarInsnNode(Opcodes.ALOAD, 1));
	            eventHook.add(new VarInsnNode(Opcodes.FLOAD, 2));
	            eventHook.add(new MethodInsnNode(
	                    Opcodes.INVOKESTATIC, "targoss/hardcorealchemy/event/EventLivingAttack",
	                    //TODO: obfuscated environment
	                    // This may actually work in an obfuscated environment, provided we are
	                    //  dealing with the Searge name phase, since Minecraft classes are fully
	                    //  deobfuscated at that point
	                    "onLivingAttack", "(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/util/DamageSource;F)F", false
	                    ));
	            eventHook.add(new VarInsnNode(Opcodes.FSTORE, 2));
	            
	            instructions.insert(eventHook);
	        }
	    }
	    
	    ClassWriter writer = new ClassWriter(0);
	    visitor.accept(writer);
	    basicClass = writer.toByteArray();
	    
	    return basicClass;
	}
	
}
