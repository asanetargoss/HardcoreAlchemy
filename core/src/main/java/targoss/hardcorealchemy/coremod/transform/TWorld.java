/*
 * Copyright 2019 asanetargoss
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

import java.util.ListIterator;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import targoss.hardcorealchemy.coremod.MethodPatcher;
import targoss.hardcorealchemy.coremod.ObfuscatedName;

public class TWorld extends MethodPatcher {
    private static final String WORLD = "net.minecraft.world.World";
    private static final ObfuscatedName GET_WORLD_TIME = new ObfuscatedName("func_72820_D" /*getWorldTime*/);
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
        if (method.name.equals(GET_WORLD_TIME.get())) {
            InsnList instructions = method.instructions;
            ListIterator<AbstractInsnNode> iterator = instructions.iterator();
            while (iterator.hasNext()) {
                AbstractInsnNode insn = iterator.next();
                if (insn.getOpcode() == Opcodes.LRETURN) {
                    InsnList patch = new InsnList();
                    patch.add(new VarInsnNode(Opcodes.ALOAD, 0)); // world
                    patch.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                            "targoss/hardcorealchemy/util/MiscVanilla",
                            "coremodHookWorldTimeMillis",
                            "(JL" + WORLD.replace(".","/") + ";)J",
                            false));
                    instructions.insertBefore(insn, patch);
                }
            }
        }
        if (method.name.equals(EXTINGUISH_FIRE.get())) {
            InsnList patch = new InsnList();
            patch.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                    "targoss/hardcorealchemy/event/EventExtinguishFire",
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
