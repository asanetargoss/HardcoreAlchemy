/*
 * Copyright 2018 asanetargoss
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
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import targoss.hardcorealchemy.coremod.MethodPatcher;
import targoss.hardcorealchemy.coremod.ObfuscatedName;

public class TGuiContainer extends MethodPatcher {
    private static final String GUI_CONTAINER = "net.minecraft.client.gui.inventory.GuiContainer";
    private final ObfuscatedName DRAW_SLOT = new ObfuscatedName("func_146977_a" /*drawSlot*/);
    private final ObfuscatedName DRAW_SCREEN = new ObfuscatedName("func_73863_a" /*drawScreen*/);
    private final ObfuscatedName THE_SLOT = new ObfuscatedName("field_147006_u" /*theSlot*/);
    private final ObfuscatedName RETURNING_STACK = new ObfuscatedName("field_146991_C" /*returningStack*/);
    private final ObfuscatedName ENABLE_DEPTH = new ObfuscatedName("func_179126_j" /*GlStateManager.enableDepth*/);
    private final ObfuscatedName GET_STACK = new ObfuscatedName("func_75211_c" /*Slot.getStack*/);
    private final ObfuscatedName GET_HAS_STACK = new ObfuscatedName("func_75216_d" /*Slot.getHasStack*/);

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.equals(GUI_CONTAINER)) {
            return transformClass(transformedName, basicClass, ClassWriter.COMPUTE_MAXS);
        }
        return basicClass;
    }

    @Override
    public void transformMethod(MethodNode method) {
        obfuscateSlot(method);
        obfuscateHoveredTooltip(method);
        obfuscateMouseTouchItems(method);
    }

    private void obfuscateSlot(MethodNode method) {
        if (method.name.equals(DRAW_SLOT.get())) {
            InsnList instructions = method.instructions;
            ListIterator<AbstractInsnNode> iterator = instructions.iterator();
            while (iterator.hasNext()) {
                AbstractInsnNode insn = iterator.next();
                if (insn.getOpcode() == Opcodes.INVOKEVIRTUAL &&
                        ((MethodInsnNode)insn).name.equals(GET_STACK.get())) {
                    
                    // Figure out the location of the itemStack variable used for drawing the item
                    int slotItemStackVar = 4;
                    while (iterator.hasNext()) {
                        insn = iterator.next();
                        if (insn.getOpcode() == Opcodes.ASTORE) {
                            slotItemStackVar = ((VarInsnNode)insn).var;
                            break;
                        }
                    }
                    
                    // We will do our patch just before the items are drawn
                    while (iterator.hasNext()) {
                        insn = iterator.next();
                        if (insn.getOpcode() == Opcodes.INVOKESTATIC &&
                                ((MethodInsnNode)insn).name.equals(ENABLE_DEPTH.get())) {
                            break;
                        }
                    }
                    
                    // Create a patch that takes the local itemStack variable and replaces it
                    InsnList patch = new InsnList();
                    patch.add(new VarInsnNode(Opcodes.ALOAD, slotItemStackVar)); // Local itemStack variable
                    patch.add(new VarInsnNode(Opcodes.ALOAD, 1)); // Slot
                    patch.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                            "targoss/hardcorealchemy/event/EventDrawInventoryItem",
                            "onDrawItem",
                            "(Lnet/minecraft/item/ItemStack;Lnet/minecraft/inventory/Slot;)Lnet/minecraft/item/ItemStack;",
                            false));
                    patch.add(new VarInsnNode(Opcodes.ASTORE, slotItemStackVar)); // Re-store local itemStack variable
                    
                    instructions.insert(insn, patch);
                    break;
                }
            }
        }
    }
    
    private void obfuscateHoveredTooltip(MethodNode method) {
        if (method.name.equals(DRAW_SCREEN.get())) {
            InsnList instructions = method.instructions;
            ListIterator<AbstractInsnNode> iterator = instructions.iterator();
            
            // This patch is a bit tricky on 1.10, since GuiContainer.renderTooltip does not check for null ItemStacks
            //
            // Before:
            //
            // if (/* Can we render an item tooltip under normal circumstances? */)
            // {
            //     ItemStack itemstack1 = this.theSlot.getStack();
            //     this.renderToolTip(itemstack1, mouseX, mouseY);
            // }
            //
            // After:
            //
            // if (/* Can we render an item tooltip under normal circumstances? */)
            // {
            //     ItemStack itemstack1 = this.theSlot.getStack();
            //     itemStack1 = EventRenderSlotTooltip.onRenderTooltip(itemStack1, this.theSlot); // Event hook patch (applied second)
            //     if (itemStack1 == null) GOTO SKIP_TOOLTIP; // Null check patch (applied first)
            //     this.renderToolTip(itemstack1, mouseX, mouseY);
            // }
            // SKIP_TOOLTIP
            //
            LabelNode skipTooltip = null;
            
            while (iterator.hasNext()) {
                AbstractInsnNode insn = iterator.next();
                
                // Diversion 1: Figure out where we need to jump to if the itemStack ends up null
                if (insn.getOpcode() == Opcodes.INVOKEVIRTUAL && ((MethodInsnNode)insn).name.equals(GET_HAS_STACK.get())) {
                    while (iterator.hasNext()) {
                        insn = iterator.next();
                        
                        if (insn.getOpcode() == Opcodes.IFEQ) {
                            skipTooltip = ((JumpInsnNode)insn).label;
                            break;
                        }
                    }
                    continue;
                }
                
                if (skipTooltip != null &&
                        insn.getOpcode() == Opcodes.INVOKEVIRTUAL &&
                        ((MethodInsnNode)insn).name.equals(GET_STACK.get())) {
                    VarInsnNode stackFirstStoredInsn = null;
                    
                    // Diversion 2: Need to figure out where itemStack1 is, which is the variable we will use for storage
                    int stackStoreLocation = 10;
                    while (iterator.hasNext()) {
                        insn = iterator.next();
                        if ((insn.getOpcode() == Opcodes.ASTORE)) {
                            stackFirstStoredInsn = ((VarInsnNode)insn);
                            stackStoreLocation = stackFirstStoredInsn.var;
                            break;
                        }
                    }
                    if (stackFirstStoredInsn == null) {
                        // This shouldn't happen, but just in case...
                        continue;
                    }
                    
                    InsnList patch = new InsnList();
                    patch.add(new VarInsnNode(Opcodes.ALOAD, stackStoreLocation));
                    patch.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    patch.add(new FieldInsnNode(Opcodes.GETFIELD,
                            GUI_CONTAINER.replace('.', '/'),
                            THE_SLOT.get(),
                            "Lnet/minecraft/inventory/Slot;"));
                    patch.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                            "targoss/hardcorealchemy/event/EventRenderSlotTooltip",
                            "onRenderTooltip",
                            "(Lnet/minecraft/item/ItemStack;Lnet/minecraft/inventory/Slot;)Lnet/minecraft/item/ItemStack;",
                            false));
                    patch.add(new VarInsnNode(Opcodes.ASTORE, stackStoreLocation));
                    patch.add(new VarInsnNode(Opcodes.ALOAD, stackStoreLocation));
                    patch.add(new JumpInsnNode(Opcodes.IFNULL, skipTooltip));
                    
                    instructions.insert(stackFirstStoredInsn, patch);
                    break;
                }
            }
        }
    }
    
    private void obfuscateMouseTouchItems(MethodNode method) {
        if (method.name.equals(DRAW_SCREEN.get())) {
            // This time we are looking for two for clauses.
            //
            // Before:
            //
            // if (itemstack != null)
            // {
            //     // Do stuff
            // }
            // if (this.returningStack != null)
            // {
            //     // Do stuff
            // }
            //
            // After:
            //
            // if (itemstack != null)
            // {
            //     itemstack = EventDrawInventoryItem.onDrawMouseItem(itemstack);
            //     if (itemstack == null) GOTO END1;
            //     // Do stuff
            // } END1
            // if (this.returningStack != null)
            // {
            //     this.returningStack = EventDrawInventoryItem.onDrawMouseItem(this.returningStack);
            //     if (this.returningStack == null) GOTO END2;
            //     // Do stuff
            // } END2
            //
            InsnList instructions = method.instructions;
            ListIterator<AbstractInsnNode> iterator = instructions.iterator();
            AbstractInsnNode insn = null;

            // Find "if itemstack != null {..}".
            VarInsnNode itemStackLoadInsn = null;
            int itemStackVar = -1; // itemstack
            JumpInsnNode jumpToItemStackNullDest = null; // Beginning #1
            LabelNode itemStackNullDest = null; // End #1
            while (iterator.hasNext()) {
                insn = iterator.next();
                if (insn.getOpcode() == Opcodes.ALOAD) {
                    itemStackLoadInsn = (VarInsnNode)insn;
                    continue;
                }
                if (itemStackLoadInsn == null) {
                    continue;
                }
                if (insn.getOpcode() == Opcodes.IFNULL) {
                    jumpToItemStackNullDest = (JumpInsnNode)insn;
                    itemStackNullDest = jumpToItemStackNullDest.label;
                    break;
                }
            }
            itemStackVar = itemStackLoadInsn.var;
            
            // After our iteration, we are now just after "if itemstack != null {..}".
            // Find "if (this.returningStack != null)"
            boolean foundGetReturningStack = false;
            JumpInsnNode jumpToReturningStackNullDest = null; // Beginning #2
            LabelNode returningStackNullDest = null; // End #2
            while (iterator.hasNext()) {
                insn = iterator.next();
                if (insn.getOpcode() == Opcodes.GETFIELD &&
                        ((FieldInsnNode)insn).name.equals(RETURNING_STACK.get())) {
                    foundGetReturningStack = true;
                    continue;
                }
                if (!foundGetReturningStack) {
                    continue;
                }
                if (insn.getOpcode() == Opcodes.IFNULL) {
                    jumpToReturningStackNullDest = (JumpInsnNode)insn;
                    returningStackNullDest = jumpToReturningStackNullDest.label; 
                    break;
                }
            }
            
            // Patch after "if itemstack != null {"
            {
                InsnList patch = new InsnList();
                patch.add(new VarInsnNode(Opcodes.ALOAD, itemStackVar));
                patch.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                        "targoss/hardcorealchemy/event/EventDrawInventoryItem",
                        "onDrawMouseItem",
                        "(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;",
                        false));
                patch.add(new VarInsnNode(Opcodes.ASTORE, itemStackVar));
                patch.add(new VarInsnNode(Opcodes.ALOAD, itemStackVar));
                patch.add(new JumpInsnNode(Opcodes.IFNULL, itemStackNullDest));
                instructions.insert(jumpToItemStackNullDest, patch);
            }
            
            // Patch after "if this.returningStack != null {"
            {
                InsnList patch = new InsnList();
                patch.add(new VarInsnNode(Opcodes.ALOAD, 0));
                patch.add(new VarInsnNode(Opcodes.ALOAD, 0)); // Need to load twice for the PUTFIELD that comes later
                patch.add(new FieldInsnNode(Opcodes.GETFIELD,
                        GUI_CONTAINER.replace('.', '/'),
                        RETURNING_STACK.get(),
                        "Lnet/minecraft/item/ItemStack;"));
                patch.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                        "targoss/hardcorealchemy/event/EventDrawInventoryItem",
                        "onDrawMouseItem",
                        "(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;",
                        false));
                patch.add(new FieldInsnNode(Opcodes.PUTFIELD,
                        GUI_CONTAINER.replace('.', '/'),
                        RETURNING_STACK.get(),
                        "Lnet/minecraft/item/ItemStack;"));
                patch.add(new VarInsnNode(Opcodes.ALOAD, 0));
                patch.add(new FieldInsnNode(Opcodes.GETFIELD,
                        GUI_CONTAINER.replace('.', '/'),
                        RETURNING_STACK.get(),
                        "Lnet/minecraft/item/ItemStack;"));
                patch.add(new JumpInsnNode(Opcodes.IFNULL, returningStackNullDest));
                instructions.insert(jumpToReturningStackNullDest, patch);
            }
        }
    }
}
