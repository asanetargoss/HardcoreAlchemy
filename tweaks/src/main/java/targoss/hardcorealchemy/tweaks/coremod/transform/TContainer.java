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

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraftforge.common.MinecraftForge;
import targoss.hardcorealchemy.coremod.CoremodHook;
import targoss.hardcorealchemy.coremod.MethodPatcher;
import targoss.hardcorealchemy.coremod.ObfuscatedName;
import targoss.hardcorealchemy.tweaks.event.EventWorkbenchCraft;

public class TContainer extends MethodPatcher {
    protected static final String CONTAINER = "net.minecraft.inventory.Container";
    protected static final ObfuscatedName SLOT_CLICK = new ObfuscatedName("func_184996_a" /*slotClick*/);

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.equals(CONTAINER)) {
            return transformClass(transformedName, basicClass, 0);
        }
        return basicClass;
    }

    @Override
    public void transformMethod(MethodNode method) {
        if (method.name.equals(SLOT_CLICK.get())) {
            InsnList hook = new InsnList();
            hook.add(new VarInsnNode(Opcodes.ALOAD, 0)); // Container
            hook.add(new VarInsnNode(Opcodes.ALOAD, 4)); // EntityPlayer
            hook.add(new VarInsnNode(Opcodes.ILOAD, 1)); // slotId
            hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                    this.getClass().getName().replace('.', '/') + "$Hooks",
                    "onCraft",
                    "(Lnet/minecraft/inventory/Container;Lnet/minecraft/entity/player/EntityPlayer;I)V",
                    false));
            
            method.instructions.insert(hook);
        }
    }

    public static class Hooks {
        @CoremodHook
        public static void onCraft(Container container, EntityPlayer player, int slotId) {
            if (container instanceof ContainerWorkbench) {
                if (slotId >= 0) { // Need this check as Minecraft uses negative slot IDs for signaling
                    Slot slot = container.inventorySlots.get(slotId);
                    if (slot instanceof SlotCrafting) {
                        EventWorkbenchCraft event = new EventWorkbenchCraft(player, slot.getStack());
                        MinecraftForge.EVENT_BUS.post(event);
                    }
                }
            }
        }
    }
}
