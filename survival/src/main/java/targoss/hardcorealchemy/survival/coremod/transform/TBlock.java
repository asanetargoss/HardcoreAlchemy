/*
 * Copyright 2017-2022 asanetargoss
 *
 * This file is part of Hardcore Alchemy Survival.
 *
 * Hardcore Alchemy Survival is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Survival is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Survival. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.survival.coremod.transform;

import java.util.ListIterator;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import targoss.hardcorealchemy.coremod.MethodPatcher;
import targoss.hardcorealchemy.coremod.ObfuscatedName;

public class TBlock extends MethodPatcher {
    protected static final String BLOCKS = "net.minecraft.block.Block";
    protected static final String CAN_SUSTAIN_PLANT = "canSustainPlant"; // This is a Forge method
    protected static final ObfuscatedName MATERIAL_WATER = new ObfuscatedName("field_151586_h"); // Material.WATER

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.contentEquals(BLOCKS)) {
            return transformClass(transformedName, basicClass, 0);
        }
        return basicClass;
    }

    @Override
    public void transformMethod(MethodNode method) {
        // WARNING: contains return in the switch statement
        // This prevents reeds from uprooting in TaN winter (but, as a tradeoff, reeds also do not grow in winter)
        if (method.name.equals(CAN_SUSTAIN_PLANT)) {
            InsnList insns = method.instructions;
            ListIterator<AbstractInsnNode> iter = insns.iterator();
            int state = 0;
            LabelNode beachLabel = null;
            final int BEACH_LABEL_INDEX = 6;
            while (iter.hasNext()) {
                AbstractInsnNode insn = iter.next();
                switch (state) {
                case 0:
                    if (insn.getOpcode() == Opcodes.TABLESWITCH && ((TableSwitchInsnNode)insn).labels.size() >= BEACH_LABEL_INDEX + 1) {
                        beachLabel = ((TableSwitchInsnNode)insn).labels.get(BEACH_LABEL_INDEX);
                        ++state;
                    }
                    break;
                case 1:
                    if ((insn instanceof LabelNode) && insn == beachLabel) {
                        ++state;
                    }
                    break;
                case 2:
                    if (insn.getOpcode() == Opcodes.GETSTATIC && ((FieldInsnNode)insn).name.equals(MATERIAL_WATER.get())) {
                        ++state;
                    }
                    break;
                case 3:
                    if (insn.getOpcode() == Opcodes.IF_ACMPEQ) {
                        InsnList patch = new InsnList();
                        patch.add(new VarInsnNode(Opcodes.ALOAD, 2)); // IBlockAccess
                        patch.add(new VarInsnNode(Opcodes.ALOAD, 3)); // BlockPos
                        patch.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                                "targoss/hardcorealchemy/survival/listener/ListenerCrops",
                                "hasWaterAlternative",
                                "(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;)Z",
                                false));
                        patch.add(new JumpInsnNode(Opcodes.IFNE, ((JumpInsnNode)insn).label));
                        insns.insert(insn, patch);
                        return;
                    }
                    break;
                }
            }
        }
    }

}
