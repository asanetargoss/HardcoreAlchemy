/*
 * Copyright 2017-2022 asanetargoss
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

import javax.annotation.Nullable;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.minecraft.item.ItemStack;
import targoss.hardcorealchemy.coremod.CoremodHook;
import targoss.hardcorealchemy.coremod.MethodPatcher;
import targoss.hardcorealchemy.coremod.ObfuscatedName;
import targoss.hardcorealchemy.tweaks.listener.ListenerEntityVoidfade;

public class TInventoryPlayer extends MethodPatcher {
    protected static final String INVENTORY_PLAYER = "net.minecraft.entity.player.InventoryPlayer";
    protected static final ObfuscatedName SET_INVENTORY_SLOT_CONTENTS = new ObfuscatedName("func_70299_a" /*setInventorySlotContents*/);
    protected static final ObfuscatedName ADD_ITEM_STACK_TO_INVENTORY = new ObfuscatedName("func_70441_a" /*addItemStackToInventory*/);
    protected static final ObfuscatedName CAN_MERGE_STACKS = new ObfuscatedName("func_184436_a" /*canMergeStacks*/);

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
        else if (method.name.equals(ADD_ITEM_STACK_TO_INVENTORY.get())) {
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
        else if (method.name.equals(CAN_MERGE_STACKS.get())) {
            InsnList patch = new InsnList();
            patch.add(new VarInsnNode(Opcodes.ALOAD, 1)); // ItemStack currently in the slot (may be empty)
            patch.add(new VarInsnNode(Opcodes.ALOAD, 2)); // ItemStack to be merged into in the slot (non-empty)
            patch.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                    this.getClass().getName().replace('.', '/') + "$Hooks",
                    "onCheckMergeStacks",
                    "(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z",
                    false));
            LabelNode keepCheckingMergeStacks = new LabelNode();
            patch.add(new JumpInsnNode(Opcodes.IFEQ, keepCheckingMergeStacks));
            patch.add(new InsnNode(Opcodes.ICONST_1));
            patch.add(new InsnNode(Opcodes.IRETURN));
            patch.add(keepCheckingMergeStacks);
            patch.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
            method.instructions.insert(patch);
        }
    }
    
    public static class Hooks {
        @CoremodHook
        public static boolean onCheckMergeStacks(@Nullable ItemStack stackInSlot, ItemStack incomingStack) {
            return ListenerEntityVoidfade.canMergeQuartzStacks(stackInSlot, incomingStack);
        }
    }
}
