/*
 * Copyright 2017-2022 asanetargoss
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

import java.util.ArrayList;
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

public class TCraftingManager extends MethodPatcher {
    private static final String CRAFTING_MANAGER = "net.minecraft.item.crafting.CraftingManager";
    private static final ObfuscatedName FIND_MATCHING_RECIPE = new ObfuscatedName("func_82787_a" /*findMatchingRecipe*/);
    
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.equals(CRAFTING_MANAGER)) {
            return transformClass(transformedName, basicClass, ClassWriter.COMPUTE_MAXS);
        }
        return basicClass;
    }
    
    @Override
    public void transformMethod(MethodNode method) {
        if (method.name.equals(FIND_MATCHING_RECIPE.get())) {
            InsnList code = method.instructions;
            
            // Locate any and all return statements
            ArrayList<InsnNode> entryPoints = new ArrayList<InsnNode>();
            ListIterator<AbstractInsnNode> iterator = code.iterator();
            while (iterator.hasNext()) {
                AbstractInsnNode insn = iterator.next();
                if (insn instanceof InsnNode && insn.getOpcode() == Opcodes.ARETURN) {
                    entryPoints.add((InsnNode)insn);
                }
            }
            
            /* Insert an event hook just before each return
             * statement so the event is allowed to modify
             * the itemstack
             * Instruction objects must be unique
             */
            for (InsnNode entryPoint : entryPoints) {
                InsnList hook = new InsnList();
                // Just before the return statement, an ItemStack (possibly null) will be on the stack
                hook.add(new VarInsnNode(Opcodes.ALOAD, 1)); // InventoryCrafting
                hook.add(new VarInsnNode(Opcodes.ALOAD, 2)); // World
                hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                        "targoss/hardcorealchemy/event/EventCraftPredict", 
                        "onCraftPredict",
                        "(Lnet/minecraft/item/ItemStack;Lnet/minecraft/inventory/InventoryCrafting;" +
                        "Lnet/minecraft/world/World;)Lnet/minecraft/item/ItemStack;",
                        false)); // EventCraftPredict event hook call
                
                code.insertBefore(entryPoint, hook);
            }
        }
    }
}
