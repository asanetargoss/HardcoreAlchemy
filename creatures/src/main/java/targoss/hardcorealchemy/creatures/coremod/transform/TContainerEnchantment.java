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
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerEnchantment;
import net.minecraft.item.ItemStack;
import targoss.hardcorealchemy.coremod.MethodPatcher;
import targoss.hardcorealchemy.coremod.ObfuscatedName;
import targoss.hardcorealchemy.event.EventEnchant;

public class TContainerEnchantment extends MethodPatcher {
    protected static final String CONTAINER_ENCHANTMENT = "net.minecraft.inventory.ContainerEnchantment";
    protected static final ObfuscatedName ENCHANT_ITEM = new ObfuscatedName("func_75140_a" /*enchantItem*/);
    // Need two method name checks to account for Minecraft code differences in dev vs release - this rarely happens!
    protected static final ObfuscatedName ADD_STAT = new ObfuscatedName("func_71064_a" /*EntityPlayer.addStat*/);
    protected static final ObfuscatedName ADD_STAT_2 = new ObfuscatedName("func_71029_a" /*EntityPlayer.addStat*/);
    
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.equals(CONTAINER_ENCHANTMENT)) {
            return transformClass(transformedName, basicClass, 0);
        }
        return basicClass;
    }

    @Override
    public void transformMethod(MethodNode method) {
        if (method.name.equals(ENCHANT_ITEM.get())) {
            ListIterator<AbstractInsnNode> it = method.instructions.iterator();
            while (it.hasNext()) {
                AbstractInsnNode insn = it.next();
                if (insn.getOpcode() != Opcodes.INVOKEVIRTUAL) {
                    continue;
                }
                if (!(insn instanceof MethodInsnNode)) {
                    continue;
                }
                String methodName = ((MethodInsnNode)insn).name;
                if (!( methodName.equals(  ADD_STAT.get()) ||
                       methodName.equals(ADD_STAT_2.get())    )) {
                    continue;
                }
                
                InsnList hook = new InsnList();
                hook.add(new VarInsnNode(Opcodes.ALOAD, 1)); // EntityPlayer
                hook.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this (ContainerEnchantment)
                hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                        this.getClass().getName().replace('.', '/') + "$Hooks",
                        "onEnchantPost",
                        "(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/inventory/ContainerEnchantment;)V",
                        false));
                method.instructions.insert(insn, hook);
            }
        }
    }

    public static class Hooks {
        public static void onEnchantPost(EntityPlayer player, ContainerEnchantment container) {
            ItemStack enchantStack = container.tableInventory.getStackInSlot(0);
            enchantStack = EventEnchant.onEnchantPost(player, enchantStack);
            container.tableInventory.setInventorySlotContents(0, enchantStack);
        }
    }
}
