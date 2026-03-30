/*
 * Copyright 2017-2026 asanetargoss
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

import java.util.ListIterator;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.minecraft.entity.projectile.EntityArrow;
import targoss.hardcorealchemy.coremod.MethodPatcher;
import targoss.hardcorealchemy.coremod.ObfuscatedName;
import targoss.hardcorealchemy.event.EventArrowUpdate;

public class TEntityArrow extends MethodPatcher {
    private static final String ENTITY_ARROW = "net.minecraft.entity.projectile.EntityArrow";
    private static final ObfuscatedName ON_UPDATE = new ObfuscatedName("func_70071_h_" /*onUpdate*/);

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.equals(ENTITY_ARROW)) {
            return transformClass(transformedName, basicClass, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        }
        return basicClass;
    }

    @Override
    public void transformMethod(MethodNode method) {
        if (method.name.equals(ON_UPDATE.get())) {
            InsnList insns = method.instructions;
            ListIterator<AbstractInsnNode> it = insns.iterator();
            while (it.hasNext()) {
                AbstractInsnNode insn = it.next();
                if (insn.getOpcode() == Opcodes.INVOKESPECIAL && ((MethodInsnNode)insn).name.equals(ON_UPDATE.get())) {
                    InsnList hook = new InsnList();
                    hook.add(new VarInsnNode(Opcodes.ALOAD, 0)); // EntityArrow
                    hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                            this.getClass().getName().replace('.', '/') + "$Hooks",
                            "onArrowUpdate",
                            "(L" + ENTITY_ARROW.replace('.', '/') + ";)Z",
                            false));
                    LabelNode continueUpdate = new LabelNode();
                    hook.add(new JumpInsnNode(Opcodes.IFNE, continueUpdate));
                    hook.add(continueUpdate);
                    insns.insert(insn, hook);
                }
            }
        }
    }
    
    public static class Hooks {
        public static boolean onArrowUpdate(EntityArrow arrow) {
            return EventArrowUpdate.onArrowUpdate(arrow);
        }
    }
}
