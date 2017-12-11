package targoss.hardcorealchemy.coremod.transform;

import java.util.ListIterator;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import targoss.hardcorealchemy.coremod.MethodPatcher;
import targoss.hardcorealchemy.coremod.ObfuscatedName;

public class TProjectEKeyHandler extends MethodPatcher {
    // Anonymous Runnable class inside of Handler, which is an instance of simpleimpl's IMessageHandler
    private static final String HANDLER_CLASS = "moze_intel.projecte.network.packets.KeyPressPKT$Handler$1";
    private static final ObfuscatedName PLAYER_ENTITY = new ObfuscatedName("playerEntity", "field_147369_b");

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.equals(HANDLER_CLASS)) {
            return transformClass(transformedName, basicClass, ClassWriter.COMPUTE_MAXS);
        }
        return basicClass;
    }
    
    @Override
    public void transformMethod(MethodNode method) {
        if (method.name.equals("run")) {
            ListIterator<AbstractInsnNode> iterator = method.instructions.iterator();
            while (iterator.hasNext()) {
                AbstractInsnNode insn = iterator.next();
                if (insn instanceof FieldInsnNode &&
                        ((FieldInsnNode)insn).name.equals(PLAYER_ENTITY.get())) {
                    InsnList insnList = new InsnList();
                    insnList.add(new InsnNode(Opcodes.DUP)); // Get our own copy of EntityPlayer
                    insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                            "targoss/hardcorealchemy/event/Hooks",
                            "canUseProjectEKeybinds",
                            "(Lnet/minecraft/entity/player/EntityPlayerMP;)Z",
                            false)); // canUseHighMagic boolean hook
                    // return out of the function if the hook returns false
                    LabelNode executeHighMagic = new LabelNode();
                    insnList.add(new JumpInsnNode(Opcodes.IFNE, executeHighMagic));
                    insnList.add(new InsnNode(Opcodes.POP)); // Pop unstored EntityPlayer
                    insnList.add(new InsnNode(Opcodes.RETURN));
                    insnList.add(executeHighMagic);
                    insnList.add(new FrameNode(Opcodes.F_FULL,
                            1, new Object[]{"moze_intel/projecte/network/packets/KeyPressPKT$Handler$1"},
                            1, new Object[]{"net/minecraft/entity/player/EntityPlayerMP"}
                            ));
                    
                    method.instructions.insert(insn, insnList);
                }
            }
        }
    }
}
