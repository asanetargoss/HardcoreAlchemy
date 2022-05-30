/*
 * Copyright 2017-2022 asanetargoss
 *
 * This file is part of Hardcore Alchemy Core.
 *
 * Hardcore Alchemy Core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Core is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Core. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.coremod.transform;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import targoss.hardcorealchemy.coremod.MethodPatcher;

/**
 * Adds a check to disable the thirst overlay when incorporeal
 */
public class TThirstOverlayHandler extends MethodPatcher {
    private static final String THIRST_OVERLAY_HANDLER = "toughasnails.handler.thirst.ThirstOverlayHandler";
    private static final String ON_PRE_RENDER_OVERLAY = "onPreRenderOverlay";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.equals(THIRST_OVERLAY_HANDLER)) {
            return transformClass(transformedName, basicClass, ClassWriter.COMPUTE_MAXS + ClassWriter.COMPUTE_FRAMES);
        }
        return basicClass;
    }

    @Override
    public void transformMethod(MethodNode method) {
        if (method.name.equals(ON_PRE_RENDER_OVERLAY)) {
            insertThirstCheck(method);
        }
    }
    
    /**
     * Part of dissolution thirst overlay visibility fix
     */
    public void insertThirstCheck(MethodNode method) {
        InsnList patch = new InsnList();
        
        // ListenerGuiHud.clientHasThirst()
        patch.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                "targoss/hardcorealchemy/event/MiscHooks$ClientSide",
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
