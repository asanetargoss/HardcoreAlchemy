/*
 * Copyright 2017-2026 asanetargoss
 *
 * This file is part of Hardcore Alchemy Core.
 *
 * Hardcore Alchemy Core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Core is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Core. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.coremod.transform;

import java.util.ListIterator;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import targoss.hardcorealchemy.coremod.MethodPatcher;
import targoss.hardcorealchemy.coremod.ObfuscatedName;
import targoss.hardcorealchemy.event.EventCraftPredict;

public class TInventoryCraftBook extends MethodPatcher {
    private static final String INVENTORY_CRAFT_BOOK = "net.blay09.mods.cookingforblockheads.container.inventory.InventoryCraftBook";
    private static final String TRY_CRAFT = "tryCraft";
    private static final ObfuscatedName GET_CRAFTING_RESULT = new ObfuscatedName("func_77572_b" /*getCraftingResult*/);

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.equals(INVENTORY_CRAFT_BOOK)) {
            return transformClass(transformedName, basicClass, ClassWriter.COMPUTE_MAXS);
        }
        return basicClass;
    }

    @Override
    public void transformMethod(MethodNode method) {
        if (method.name.equals(TRY_CRAFT)) {
            InsnList code = method.instructions;
            ListIterator<AbstractInsnNode> iterator = code.iterator();
            while (iterator.hasNext()) {
                AbstractInsnNode insn = iterator.next();
                if ((insn instanceof MethodInsnNode) && ((MethodInsnNode)insn).name.equals(GET_CRAFTING_RESULT.get())) {
                    InsnList hook = new InsnList();
                    hook.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this (InventoryCraftBook instanceof InventoryCrafting)
                    hook.add(new VarInsnNode(Opcodes.ALOAD, 3)); // EntityPlayer
                    hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                            this.getClass().getName().replace('.', '/') + "$Hooks",
                            "onCraftPredict",
                            "(Lnet/minecraft/item/ItemStack;Lnet/minecraft/inventory/InventoryCrafting;" +
                            "Lnet/minecraft/entity/player/EntityPlayer;)Lnet/minecraft/item/ItemStack;",
                            false));
                    code.insert(insn, hook);
                }
            }
        }
    }
    
    public static class Hooks {
        public static ItemStack onCraftPredict(ItemStack craftResult, InventoryCrafting craftGrid, EntityPlayer player) {
            return EventCraftPredict.onCraftPredict(craftResult, craftGrid, player.world);
        }
    }
}
