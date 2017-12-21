package targoss.hardcorealchemy.coremod.transform;

import java.util.HashSet;
import java.util.ListIterator;
import java.util.Set;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import targoss.hardcorealchemy.coremod.MethodPatcher;

public class TThirstOverlayHandler extends MethodPatcher {
    private static final String THIRST_OVERLAY_HANDLER = "toughasnails.handler.thirst.ThirstOverlayHandler";
    private static final Set<String> OVERLAY_EVENTS = new HashSet<>();
    
    static {
        OVERLAY_EVENTS.add("onClientTick");
        OVERLAY_EVENTS.add("onPreRenderOverlay");
        OVERLAY_EVENTS.add("onPostRenderOverlay");
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.equals(THIRST_OVERLAY_HANDLER)) {
            return transformClass(transformedName, basicClass, ClassWriter.COMPUTE_MAXS + ClassWriter.COMPUTE_FRAMES);
        }
        return basicClass;
    }

    @Override
    public void transformMethod(MethodNode method) {
        if (OVERLAY_EVENTS.contains(method.name)) {
            InsnList patch = new InsnList();
            // ListenerGuiHud.clientHasThirst()
            patch.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                    "targoss/hardcorealchemy/listener/ListenerGuiHud",
                    "clientHasThirst",
                    "()Z",
                    false));
            // Should we render thirst? If not, return.
            LabelNode renderThirst = new LabelNode();
            patch.add(new JumpInsnNode(Opcodes.IFNE, renderThirst));
            patch.add(new InsnNode(Opcodes.RETURN));
            patch.add(renderThirst);
            
            method.instructions.insert(patch);
        }
    }

}
