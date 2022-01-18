/*
 * Copyright 2021 asanetargoss
 * 
 * This file is part of the Hardcore Alchemy capstone mod.
 * 
 * The Hardcore Alchemy capstone mod is free software: you can redistribute
 * it and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 3 of the
 * License.
 * 
 * The Hardcore Alchemy capstone mod is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the Hardcore Alchemy capstone mod. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.tweaks.coremod.transform;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import targoss.hardcorealchemy.coremod.MethodPatcher;
import targoss.hardcorealchemy.coremod.ObfuscatedName;

public class TInventoryPlayer extends MethodPatcher {
    protected static final String INVENTORY_PLAYER = "net.minecraft.entity.player.InventoryPlayer";
    protected static final ObfuscatedName SET_INVENTORY_SLOT_CONTENTS = new ObfuscatedName("func_70299_a" /*setInventorySlotContents*/);
    protected static final ObfuscatedName ADD_ITEM_STACK_TO_INVENTORY = new ObfuscatedName("func_70441_a" /*addItemStackToInventory*/);

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.equals(INVENTORY_PLAYER)) {
            return transformClass(transformedName, basicClass, 0);
        }
        return basicClass;
    }

    @Override
    public void transformMethod(MethodNode method) {
        if (method.name.equals(SET_INVENTORY_SLOT_CONTENTS.get())) {
            InsnList patch = new InsnList();
            patch.add(new VarInsnNode(Opcodes.ALOAD, 0)); // InventoryPlayer
            patch.add(new VarInsnNode(Opcodes.ILOAD, 1)); // int slotIndex
            patch.add(new VarInsnNode(Opcodes.ALOAD, 2)); // @Nullable ItemStack itemStack
            patch.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                    "targoss/hardcorealchemy/tweaks/event/EventPlayerInventorySlotSet",
                    "onPlayerInventorySlotSet",
                    "(Lnet/minecraft/entity/player/InventoryPlayer;ILnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;",
                    false));
            patch.add(new VarInsnNode(Opcodes.ASTORE, 2));
            method.instructions.insert(patch);
        }
        if (method.name.equals(ADD_ITEM_STACK_TO_INVENTORY.get())) {
            InsnList patch = new InsnList();
            patch.add(new VarInsnNode(Opcodes.ALOAD, 0)); // InventoryPlayer
            /* NOTE: The itemStack parameter of this method is final. However,
             * it is legal to reassign local variables that have been declared
             * final, at least in Java 8. We don't need to do that here. */
            patch.add(new VarInsnNode(Opcodes.ALOAD, 1)); // @Nullable final ItemStack itemStack
            patch.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                    "targoss/hardcorealchemy/tweaks/event/EventPlayerAcquireStack",
                    "onPlayerAcquireStack",
                    "(Lnet/minecraft/entity/player/InventoryPlayer;Lnet/minecraft/item/ItemStack;)Z",
                    false));
            LabelNode keepInsertingItem = new LabelNode();
            patch.add(new JumpInsnNode(Opcodes.IFNE, keepInsertingItem));
            patch.add(new InsnNode(Opcodes.ICONST_0));
            patch.add(new InsnNode(Opcodes.IRETURN));
            patch.add(keepInsertingItem);
            patch.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
            method.instructions.insert(patch);
        }
    }

}
