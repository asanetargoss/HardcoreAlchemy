/*
 * Copyright 2017-2022 asanetargoss
 *
 * This file is part of Hardcore Alchemy Magic.
 *
 * Hardcore Alchemy Magic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Magic is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Magic. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.magic.coremod.transform;

import java.util.ListIterator;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import targoss.hardcorealchemy.coremod.MethodPatcher;
import targoss.hardcorealchemy.coremod.ObfuscatedName;

public class TProjectEKeyHandler extends MethodPatcher {
    // Anonymous Runnable class inside of Handler, which is an instance of simpleimpl's IMessageHandler
    private static final String HANDLER_CLASS = "moze_intel.projecte.network.packets.KeyPressPKT$Handler$1";
    private static final ObfuscatedName PLAYER_ENTITY = new ObfuscatedName("field_147369_b" /*playerEntity*/);

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.equals(HANDLER_CLASS)) {
            return transformClass(transformedName, basicClass, ClassWriter.COMPUTE_MAXS);
        }
        return basicClass;
    }
    
    @Override
    public void transformMethod(MethodNode method) {
        if (method.name.equals("run")) {
            ListIterator<AbstractInsnNode> iterator = method.instructions.iterator();
            while (iterator.hasNext()) {
                AbstractInsnNode insn = iterator.next();
                if (insn instanceof FieldInsnNode &&
                        ((FieldInsnNode)insn).name.equals(PLAYER_ENTITY.get())) {
                    InsnList insnList = new InsnList();
                    insnList.add(new InsnNode(Opcodes.DUP)); // Get our own copy of EntityPlayer
                    insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                            "targoss/hardcorealchemy/magic/listener/ListenerPlayerMagic",
                            "canUseProjectEKeybinds",
                            "(Lnet/minecraft/entity/player/EntityPlayerMP;)Z",
                            false)); // canUseHighMagic boolean hook
                    // return out of the function if the hook returns false
                    LabelNode executeHighMagic = new LabelNode();
                    insnList.add(new JumpInsnNode(Opcodes.IFNE, executeHighMagic));
                    insnList.add(new InsnNode(Opcodes.POP)); // Pop unstored EntityPlayer
                    insnList.add(new InsnNode(Opcodes.RETURN));
                    insnList.add(executeHighMagic);
                    insnList.add(new FrameNode(Opcodes.F_FULL,
                            1, new Object[]{"moze_intel/projecte/network/packets/KeyPressPKT$Handler$1"},
                            1, new Object[]{"net/minecraft/entity/player/EntityPlayerMP"}
                            ));
                    
                    method.instructions.insert(insn, insnList);
                }
            }
        }
    }
}
