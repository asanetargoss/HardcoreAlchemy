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

import java.util.ListIterator;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import targoss.hardcorealchemy.coremod.MethodPatcher;
import targoss.hardcorealchemy.coremod.ObfuscatedName;

public class TSlot extends MethodPatcher {
    private static final String SLOT = "net.minecraft.inventory.Slot";
    private static final ObfuscatedName CAN_TAKE_STACK = new ObfuscatedName("canTakeStack", "func_82869_a");
    
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.equals(SLOT)) {
            return transformClass(transformedName, basicClass, ClassWriter.COMPUTE_MAXS);
        }
        return basicClass;
    }
    
    @Override
    public void transformMethod(MethodNode method) {
        if (method.name.equals(CAN_TAKE_STACK.get())) {
            ListIterator<AbstractInsnNode> iterator = method.instructions.iterator();
            while (iterator.hasNext()) {
                AbstractInsnNode insn = iterator.next();
                if (insn instanceof InsnNode && ((InsnNode)insn).getOpcode() == Opcodes.ICONST_1) {
                    InsnList insnList = new InsnList();
                    insnList.add(new InsnNode(Opcodes.POP)); // Pop true boolean
                    insnList.add(new VarInsnNode(Opcodes.ALOAD, 0)); // Slot
                    insnList.add(new VarInsnNode(Opcodes.ALOAD, 1)); // EntityPlayer
                    insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                            "targoss/hardcorealchemy/event/EventTakeStack$Pre",
                            "onTakeStackPre",
                            "(Lnet/minecraft/inventory/Slot;" +
                            "Lnet/minecraft/entity/player/EntityPlayer;)Z",
                            false)); // onTakeStackPre event hook
                    method.instructions.insert(insn, insnList);
                    break;
                }
            }
        }
    }
}
