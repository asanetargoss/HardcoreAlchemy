package targoss.hardcorealchemy.coremod.transform;

import java.util.ListIterator;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import targoss.hardcorealchemy.coremod.MethodPatcher;

public class TRightClickHarvesting extends MethodPatcher {
    private static final String RIGHT_CLICK_HARVESTING = "com.pam.harvestcraft.addons.RightClickHarvesting";
    private static final String ON_PLAYER_INTERACT = "onPlayerInteract";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.equals(RIGHT_CLICK_HARVESTING)) {
            return transformClass(transformedName, basicClass, ClassWriter.COMPUTE_MAXS + ClassWriter.COMPUTE_FRAMES);
        }
        return basicClass;
    }

    @Override
    public void transformMethod(MethodNode method) {
        if (method.name.equals(ON_PLAYER_INTERACT)) {
            // Change this:
            //     if (!config.enableEasyHarvest) return;
            // To this:
            //     if (!config.enableEasyHarvest) return;
            //     if (!ListenerCrops.allowRightClickHarvest(event)) return;
            InsnList instructions = method.instructions;
            ListIterator<AbstractInsnNode> iterator = instructions.iterator();
            
            LabelNode configAllowsHarvest = null;

            int matchProgress = 0;
            while (iterator.hasNext()) {
                AbstractInsnNode insn = iterator.next();
                
                switch (matchProgress) {
                case 0:
                    if (insn.getOpcode() == Opcodes.GETFIELD &&
                            ((FieldInsnNode)insn).name.equals("enableEasyHarvest")) {
                        matchProgress++;
                    }
                break;
                case 1:
                    if (insn.getOpcode() == Opcodes.IFNE) {
                        configAllowsHarvest = ((JumpInsnNode)insn).label;
                        matchProgress++;
                    }
                break;
                case 2:
                    if (insn == configAllowsHarvest) {
                        LabelNode weAllowHarvest = new LabelNode();
                        
                        InsnList patch = new InsnList();
                        patch.add(new VarInsnNode(Opcodes.ALOAD, 1)); // event (PlayerInteractEvent.RightClickBlock)
                        patch.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                                "targoss/hardcorealchemy/listener/ListenerCrops",
                                "allowRightClickHarvest",
                                "(Lnet/minecraftforge/event/entity/player/PlayerInteractEvent$RightClickBlock;)Z",
                                false
                                ));
                        patch.add(new JumpInsnNode(Opcodes.IFNE, weAllowHarvest));
                        patch.add(new InsnNode(Opcodes.RETURN));
                        patch.add(weAllowHarvest);
                        
                        instructions.insert(insn, patch);
                        // We patched the desired instruction; now we're done.
                        return;
                    }
                break;
                }
            }
            
        }
    }

}
