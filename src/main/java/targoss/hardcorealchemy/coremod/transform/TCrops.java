package targoss.hardcorealchemy.coremod.transform;

import java.util.ListIterator;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.minecraft.launchwrapper.IClassTransformer;
import targoss.hardcorealchemy.coremod.HardcoreAlchemyCoreMod;
import targoss.hardcorealchemy.coremod.ObfuscatedName;

/**
 * Quick and dirty; subject to change
 */
public class TCrops implements IClassTransformer {
    private static final String PAM_CROP = "com.pam.harvestcraft.blocks.growables.BlockPamCrop";
    private static final String MYSICAL_CROP = "com.blakebr0.mysticalagriculture.blocks.crop.BlockMysticalCrop";
    private static final String PAM_FRUIT = "com.pam.harvestcraft.blocks.growables.BlockPamFruit";
    private static final String PAM_FRUIT_LOG = "com.pam.harvestcraft.blocks.growables.BlockPamFruitLog";
    private static final ObfuscatedName UPDATE_TICK = new ObfuscatedName("updateTick", "func_180650_b");

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.equals(PAM_CROP) || transformedName.equals(MYSICAL_CROP)) {
            return transformDecay(transformedName, HardcoreAlchemyCoreMod.obfuscated, basicClass);
        }
        else if (transformedName.equals(PAM_FRUIT) || transformedName.equals(PAM_FRUIT_LOG)) {
            return transformHibernate(transformedName, HardcoreAlchemyCoreMod.obfuscated, basicClass);
        }
        return basicClass;
    }
    
    private byte[] transformDecay(String name, boolean obfuscated, byte[] basicClass) {
        ClassReader reader = new ClassReader(basicClass);
        ClassNode visitor = new ClassNode();
        reader.accept(visitor, 0);
        
        for (MethodNode method : visitor.methods) {
            if (method.name.equals(UPDATE_TICK.get())) {
                InsnList instructions = method.instructions;
                ListIterator<AbstractInsnNode> iterator = instructions.iterator();
                while (iterator.hasNext()) {
                    AbstractInsnNode insn = iterator.next();
                    if (insn.getOpcode() == Opcodes.RETURN) {
                        InsnList hook = new InsnList();
                        hook.add(new VarInsnNode(Opcodes.ALOAD, 3)); //IBlockState
                        hook.add(new VarInsnNode(Opcodes.ALOAD, 1)); //World
                        hook.add(new VarInsnNode(Opcodes.ALOAD, 2)); //BlockPos
                        hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                                "targoss/hardcorealchemy/listener/ListenerCrops",
                                "checkDecay",
                                "(Lnet/minecraft/block/state/IBlockState;" +
                                "Lnet/minecraft/world/World;" +
                                "Lnet/minecraft/util/math/BlockPos;"+
                                ")V",
                                false
                                ));
                        
                        instructions.insertBefore(insn, hook);
                    }
                }
            }
        }
        
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        visitor.accept(writer);
        byte[] newClass = writer.toByteArray();
        
        return newClass;
    }
    
    private byte[] transformHibernate(String name, boolean obfuscated, byte[] basicClass) {
        ClassReader reader = new ClassReader(basicClass);
        ClassNode visitor = new ClassNode();
        reader.accept(visitor, 0);
        
        for (MethodNode method : visitor.methods) {
            if (method.name.equals(UPDATE_TICK.get())) {
                InsnList instructions = method.instructions;
                InsnList hook = new InsnList();
                hook.add(new VarInsnNode(Opcodes.ALOAD, 3)); //IBlockState
                hook.add(new VarInsnNode(Opcodes.ALOAD, 1)); //World
                hook.add(new VarInsnNode(Opcodes.ALOAD, 2)); //BlockPos
                hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                        "targoss/hardcorealchemy/listener/ListenerCrops",
                        "shouldHibernate",
                        "(Lnet/minecraft/block/state/IBlockState;" +
                        "Lnet/minecraft/world/World;" +
                        "Lnet/minecraft/util/math/BlockPos;"+
                        ")Z",
                        false
                        ));
                LabelNode dontHibernate = new LabelNode();
                hook.add(new JumpInsnNode(Opcodes.IFEQ, dontHibernate));
                hook.add(new InsnNode(Opcodes.RETURN));
                hook.add(dontHibernate);
                
                instructions.insert(hook);
            }
        }
        
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS + ClassWriter.COMPUTE_FRAMES);
        visitor.accept(writer);
        byte[] newClass = writer.toByteArray();
        
        return newClass;
    }
}
