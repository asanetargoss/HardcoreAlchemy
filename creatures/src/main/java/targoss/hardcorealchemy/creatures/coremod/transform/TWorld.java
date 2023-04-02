/*
 * Copyright 2017-2023 asanetargoss
 *
 * This file is part of Hardcore Alchemy Creatures.
 *
 * Hardcore Alchemy Creatures is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Creatures is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Creatures. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.creatures.coremod.transform;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import targoss.hardcorealchemy.coremod.MethodPatcher;
import targoss.hardcorealchemy.coremod.ObfuscatedName;

public class TWorld extends MethodPatcher {
    private static final String WORLD = "net.minecraft.world.World";
    private static final ObfuscatedName EXTINGUISH_FIRE = new ObfuscatedName("func_175719_a" /*extinguishFire*/);

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.equals(WORLD)) {
            return transformClass(transformedName, basicClass, ClassWriter.COMPUTE_MAXS);
        }
        return basicClass;
    }

    @Override
    public void transformMethod(MethodNode method) {
        if (method.name.equals(EXTINGUISH_FIRE.get())) {
            InsnList patch = new InsnList();
            patch.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                    "targoss/hardcorealchemy/creatures/event/EventExtinguishFire",
                    "onExtinguishFire",
                    "()Z",
                    false));
            LabelNode businessAsUsual = new LabelNode();
            patch.add(new JumpInsnNode(Opcodes.IFNE, businessAsUsual));
            patch.add(new LdcInsnNode(0));
            patch.add(new InsnNode(Opcodes.IRETURN));
            patch.add(businessAsUsual);
            // Not sure if we need this opcode or not
            patch.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
            
            method.instructions.insert(patch);
        }
    }

}
