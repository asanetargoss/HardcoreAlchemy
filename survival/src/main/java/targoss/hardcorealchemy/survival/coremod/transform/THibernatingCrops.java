/*
 * Copyright 2017-2023 asanetargoss
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

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import targoss.hardcorealchemy.coremod.MethodPatcher;
import targoss.hardcorealchemy.coremod.ObfuscatedName;

public class THibernatingCrops extends MethodPatcher {
    private static final String PAM_FRUIT = "com.pam.harvestcraft.blocks.growables.BlockPamFruit";
    private static final String PAM_FRUIT_LOG = "com.pam.harvestcraft.blocks.growables.BlockPamFruitLog";
    private static final String BLOCK_REED = "net.minecraft.block.BlockReed";
    private static final String BLOCK_CACTUS = "net.minecraft.block.BlockCactus";
    private static final ObfuscatedName UPDATE_TICK = new ObfuscatedName("func_180650_b" /*updateTick*/);

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.equals(PAM_FRUIT) ||
                transformedName.equals(PAM_FRUIT_LOG) ||
                transformedName.equals(BLOCK_REED) ||
                transformedName.equals(BLOCK_CACTUS)) {
            return transformClass(transformedName, basicClass, ClassWriter.COMPUTE_MAXS + ClassWriter.COMPUTE_FRAMES);
        }
        return basicClass;
    }
    
    @Override
    public void transformMethod(MethodNode method) {
        if (method.name.equals(UPDATE_TICK.get())) {
            InsnList instructions = method.instructions;
            InsnList hook = new InsnList();
            hook.add(new VarInsnNode(Opcodes.ALOAD, 3)); //IBlockState
            hook.add(new VarInsnNode(Opcodes.ALOAD, 1)); //World
            hook.add(new VarInsnNode(Opcodes.ALOAD, 2)); //BlockPos
            hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                    "targoss/hardcorealchemy/survival/listener/ListenerCrops",
                    "shouldHibernate",
                    "(Lnet/minecraft/block/state/IBlockState;" +
                    "Lnet/minecraft/world/World;" +
                    "Lnet/minecraft/util/math/BlockPos;"+
                    ")Z",
                    false
                    ));
            LabelNode dontHibernate = new LabelNode();
            hook.add(new JumpInsnNode(Opcodes.IFEQ, dontHibernate));
            hook.add(new InsnNode(Opcodes.RETURN));
            hook.add(dontHibernate);
            
            instructions.insert(hook);
        }
    }
}
