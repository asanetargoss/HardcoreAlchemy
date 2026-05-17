/*
 * Copyright 2017-2026 asanetargoss
 *
 * This file is part of Hardcore Alchemy Tweaks.
 *
 * Hardcore Alchemy Tweaks is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Tweaks is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Tweaks. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.tweaks.coremod.transform;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.minecraft.entity.Entity;
import targoss.hardcorealchemy.coremod.MethodPatcher;
import targoss.hardcorealchemy.coremod.ObfuscatedName;
import targoss.hardcorealchemy.tweaks.listener.ListenerHeartsSacrificed;

public class TEntity extends MethodPatcher {
    protected static final String ENTITY = "net.minecraft.entity.Entity";
    protected static final ObfuscatedName IS_GLOWING = new ObfuscatedName("func_184202_aL" /*isGlowing*/);

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.equals(ENTITY)) {
            return transformClass(transformedName, basicClass, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        }
        return basicClass;
    }

    @Override
    public void transformMethod(MethodNode method) {
        if (method.name.equals(IS_GLOWING.get())) {
            InsnList insns = new InsnList();
            insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
            insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                    this.getClass().getName().replace(".", "/") + "$Hooks",
                    "hasForceGlow",
                    "(Lnet/minecraft/entity/Entity;)Z",
                    false));
            LabelNode noForceGlow = new LabelNode();
            insns.add(new JumpInsnNode(Opcodes.IFEQ, noForceGlow));
            insns.add(new InsnNode(Opcodes.ICONST_1));
            insns.add(new InsnNode(Opcodes.IRETURN));
            insns.add(noForceGlow);
            
            method.instructions.insert(insns);
        }
    }

    public static class Hooks {
        public static boolean hasForceGlow(Entity entity) {
            return ListenerHeartsSacrificed.hasForceGlow(entity);
        }
    }
}
