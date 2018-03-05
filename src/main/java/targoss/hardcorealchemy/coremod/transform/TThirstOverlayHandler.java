/*
 * Copyright 2017-2018 asanetargoss
 * 
 * This file is part of Hardcore Alchemy.
 * 
 * Hardcore Alchemy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation version 3 of the License.
 * 
 * Hardcore Alchemy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Hardcore Alchemy.  If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.coremod.transform;

import java.util.HashSet;
import java.util.ListIterator;
import java.util.Set;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import targoss.hardcorealchemy.coremod.MethodPatcher;

/**
 * Adds a check to disable the thirst overlay when incorporeal
 * and changes the way the thirst overlay is rendered to be
 * more compatible with other mods.
 */
public class TThirstOverlayHandler extends MethodPatcher {
    private static final String THIRST_OVERLAY_HANDLER = "toughasnails.handler.thirst.ThirstOverlayHandler";
    private static final String ON_PRE_RENDER_OVERLAY = "onPreRenderOverlay";
    private static final String ON_POST_RENDER_OVERLAY = "onPostRenderOverlay";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.equals(THIRST_OVERLAY_HANDLER)) {
            return transformClass(transformedName, basicClass, ClassWriter.COMPUTE_MAXS + ClassWriter.COMPUTE_FRAMES);
        }
        return basicClass;
    }

    @Override
    public void transformMethod(MethodNode method) {
        if (method.name.equals(ON_POST_RENDER_OVERLAY)) {
            insertUnconditionalReturn(method);
        }
        
        if (method.name.equals(ON_PRE_RENDER_OVERLAY)) {
            addThirstFixAnnotations(method);
            insertThirstFixDivert(method);
            insertThirstCheck(method);
        }
    }
    
    /**
     * Part of thirst overlay render compatibility fix.
     */
    public void insertUnconditionalReturn(MethodNode method) {
        InsnList patch = new InsnList();
        patch.add(new InsnNode(Opcodes.RETURN));
        method.instructions.insert(patch);
    }
    
    /**
     * Part of thirst overlay render compatibility fix.
     */
    public void addThirstFixAnnotations(MethodNode method) {
        for (AnnotationNode annotation : method.visibleAnnotations) {
            if (annotation.desc.equals("Lnet/minecraftforge/fml/common/eventhandler/SubscribeEvent;")) {
                annotation.visitEnum("priority", "Lnet/minecraftforge/fml/common/eventhandler/EventPriority;", "HIGHEST");
                annotation.visit("receiveCanceled", new Boolean(true));
            }
        }
    }
    
    /**
     * Part of thirst overlay render compatibility fix
     */
    public void insertThirstFixDivert(MethodNode method) {
        InsnList patch = new InsnList();
        
        // ListenerGuiHud.onRenderThirst
        patch.add(new VarInsnNode(Opcodes.ALOAD, 1));
        patch.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                "targoss/hardcorealchemy/listener/ListenerGuiHud",
                "onRenderThirst",
                "(Lnet/minecraftforge/client/event/RenderGameOverlayEvent$Pre;)V",
                false));
        // return
        patch.add(new InsnNode(Opcodes.RETURN));
        
        method.instructions.insert(patch);
    }
    
    /**
     * Part of dissolution thirst overlay visibility fix
     */
    public void insertThirstCheck(MethodNode method) {
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
