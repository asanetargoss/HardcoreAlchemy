/*
 * Copyright 2017-2022 asanetargoss
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
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import targoss.hardcorealchemy.coremod.MethodPatcher;
import targoss.hardcorealchemy.coremod.ObfuscatedName;

public class TEntityPlayerSP extends MethodPatcher {
    private static final String ENTITY_PLAYER_SP = "net.minecraft.client.entity.EntityPlayerSP";
    private final ObfuscatedName UPDATE_AUTO_JUMP = new ObfuscatedName("func_189810_i" /*updateAutoJump*/);
    private final ObfuscatedName IS_SNEAKING = new ObfuscatedName("func_70093_af" /*isSneaking*/);

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.equals(ENTITY_PLAYER_SP)) {
            return transformClass(transformedName, basicClass, 0);
        }
        return basicClass;
    }

    @Override
    public void transformMethod(MethodNode method) {
        if (method.name.equals(UPDATE_AUTO_JUMP.get())) {
            InsnList instructions = method.instructions;
            ListIterator<AbstractInsnNode> iterator = instructions.iterator();
            while (iterator.hasNext()) {
                AbstractInsnNode insn = iterator.next();
                if (insn.getOpcode() == Opcodes.INVOKEVIRTUAL &&
                        ((MethodInsnNode)insn).name.equals(IS_SNEAKING.get())) {
                    InsnList patch = new InsnList();
                    patch.add(new InsnNode(Opcodes.POP));
                    patch.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    patch.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                            "targoss/hardcorealchemy/creatures/listener/ListenerPlayerHinderedMind",
                            "isPlayerSneakingToPreventAutoJump",
                            "(Lnet/minecraft/entity/player/EntityPlayer;)Z",
                            false));
                    
                    instructions.insert(insn, patch);
                    break;
                }
            }
        }
    }

}
