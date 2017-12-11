package targoss.hardcorealchemy.coremod.transform;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import targoss.hardcorealchemy.coremod.MethodPatcher;
import targoss.hardcorealchemy.coremod.ObfuscatedName;

public class THibernatingCrops extends MethodPatcher {
    private static final String PAM_FRUIT = "com.pam.harvestcraft.blocks.growables.BlockPamFruit";
    private static final String PAM_FRUIT_LOG = "com.pam.harvestcraft.blocks.growables.BlockPamFruitLog";
    private static final ObfuscatedName UPDATE_TICK = new ObfuscatedName("updateTick", "func_180650_b");

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.equals(PAM_FRUIT) || transformedName.equals(PAM_FRUIT_LOG)) {
            return transformClass(transformedName, basicClass, ClassWriter.COMPUTE_MAXS + ClassWriter.COMPUTE_FRAMES);
        }
        return basicClass;
    }
    
    @Override
    public void transformMethod(MethodNode method) {
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
}
