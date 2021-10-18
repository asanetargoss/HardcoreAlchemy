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

package targoss.hardcorealchemy.survival.coremod.transform;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import targoss.hardcorealchemy.coremod.MethodPatcher;
import targoss.hardcorealchemy.coremod.ObfuscatedName;

public class TBlockReed extends MethodPatcher {
    protected static final String BLOCK_REED = "net.minecraft.block.BlockReed";
    protected static final ObfuscatedName CAN_BLOCK_STAY = new ObfuscatedName("func_176354_d" /* canBlockStay */);
    protected static final ObfuscatedName UPDATE_TICK = new ObfuscatedName("func_180650_b" /* updateTick */);
    protected static final ObfuscatedName CHECK_FOR_DROP = new ObfuscatedName("func_176353_e" /* checkForDrop */);

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.equals(BLOCK_REED)) {
            return transformClass(transformedName, basicClass, 0);
        }
        return basicClass;
    }

    @Override
    public void transformMethod(MethodNode method) {
        if (method.name.equals(CAN_BLOCK_STAY.get())) {
            InsnList patch = new InsnList();
            // if (canReedStay(...)) return true;
            patch.add(new VarInsnNode(Opcodes.ALOAD, 0)); // Block
            patch.add(new VarInsnNode(Opcodes.ALOAD, 1)); // World
            patch.add(new VarInsnNode(Opcodes.ALOAD, 2)); // BlockPos
            patch.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                    "targoss/hardcorealchemy/survival/listener/ListenerCrops",
                    "canReedStay",
                    "(Lnet/minecraft/block/Block;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Z",
                    false));
            patch.add(new InsnNode(Opcodes.IRETURN));
            method.instructions.insert(patch);
        }
        if (method.name.equals(UPDATE_TICK.get())) {
            // Override the reed's quick return when the block directly beneath it is a reed, by calling checkForDrop again earlier
            InsnList patch = new InsnList();
            patch.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this
            patch.add(new VarInsnNode(Opcodes.ALOAD, 1)); // World
            patch.add(new VarInsnNode(Opcodes.ALOAD, 2)); // BlockPos
            patch.add(new VarInsnNode(Opcodes.ALOAD, 3)); // IBlockState
            patch.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
                    "net/minecraft/block/BlockReed",
                    CHECK_FOR_DROP.get(),
                    "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)Z",
                    false));
            method.instructions.insert(patch);
        }
    }

}
