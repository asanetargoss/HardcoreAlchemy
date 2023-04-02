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

import java.util.ListIterator;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import targoss.hardcorealchemy.coremod.MethodPatcher;
import targoss.hardcorealchemy.coremod.ObfuscatedName;

public class TEntity extends MethodPatcher {
    private static final String ENTITY = "net.minecraft.entity.Entity";
    private static final ObfuscatedName MOVE = new ObfuscatedName("func_70091_d" /*move*/);

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.equals(ENTITY)) {
            return transformClass(transformedName, basicClass, 0);
        }
        return basicClass;
    }

    @Override
    public void transformMethod(MethodNode method) {
        if (method.name.equals(MOVE.get())) {
            InsnList instructions = method.instructions;
            ListIterator<AbstractInsnNode> iterator = instructions.iterator();
            while (iterator.hasNext()) {
                AbstractInsnNode insn = iterator.next();
                if (insn.getOpcode() == Opcodes.INSTANCEOF &&
                        ((TypeInsnNode)insn).desc.equals("net/minecraft/entity/player/EntityPlayer")) {
                    // The first instance check of EntityPlayer is to decide if the player should be sneaking.
                    // Let's disable sneaking if the player is mentally hindered, by skipping the block of code
                    
                    AbstractInsnNode jumpInsn = iterator.next();
                    if (jumpInsn.getOpcode() != Opcodes.IFEQ) {
                        // Sanity check
                        continue;
                    }
                    
                    InsnList patch = new InsnList();
                    patch.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    patch.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                            "targoss/hardcorealchemy/creatures/listener/ListenerPlayerHinderedMind",
                            "canPlayerUseSneakToPreventFall",
                            "(Lnet/minecraft/entity/Entity;)Z",
                            false));
                    patch.add(new JumpInsnNode(Opcodes.IFEQ, ((JumpInsnNode)jumpInsn).label));
                    
                    instructions.insert(jumpInsn, patch);
                    break;
                }
            }
        }
    }

}
