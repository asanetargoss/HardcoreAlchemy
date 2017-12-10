package targoss.hardcorealchemy.coremod.transform;

import java.util.ListIterator;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.minecraft.launchwrapper.IClassTransformer;
import targoss.hardcorealchemy.coremod.HardcoreAlchemyCoreMod;
import targoss.hardcorealchemy.coremod.ObfuscatedName;

public class TSlot implements IClassTransformer {
    private static final String SLOT = "net.minecraft.inventory.Slot";
    private static final ObfuscatedName CAN_TAKE_STACK = new ObfuscatedName("canTakeStack", "func_82869_a");
    
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.equals(SLOT)) {
            return transformClass(transformedName, HardcoreAlchemyCoreMod.obfuscated, basicClass);
        }
        return basicClass;
    }
    
    private byte[] transformClass(String name, boolean obfuscated, byte[] basicClass) {
        ClassReader reader = new ClassReader(basicClass);
        ClassNode visitor = new ClassNode();
        reader.accept(visitor, 0);
        
        for (MethodNode method : visitor.methods) {
            if (method.name.equals(CAN_TAKE_STACK.get())) {
                ListIterator<AbstractInsnNode> iterator = method.instructions.iterator();
                while (iterator.hasNext()) {
                    AbstractInsnNode insn = iterator.next();
                    if (insn instanceof InsnNode && ((InsnNode)insn).getOpcode() == Opcodes.ICONST_1) {
                        InsnList insnList = new InsnList();
                        insnList.add(new InsnNode(Opcodes.POP)); // Pop true boolean
                        insnList.add(new VarInsnNode(Opcodes.ALOAD, 0)); // Slot
                        insnList.add(new VarInsnNode(Opcodes.ALOAD, 1)); // EntityPlayer
                        insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                                "targoss/hardcorealchemy/event/EventTakeStack$Pre",
                                "onTakeStackPre",
                                "(Lnet/minecraft/inventory/Slot;" +
                                "Lnet/minecraft/entity/player/EntityPlayer;)Z",
                                false)); // onTakeStackPre event hook
                        method.instructions.insert(insn, insnList);
                        break;
                    }
                }
            }
        }
        
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        visitor.accept(writer);
        basicClass = writer.toByteArray();
        
        return basicClass;
    }
}
