/*
 * Copyright 2017-2018 asanetargoss
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
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import targoss.hardcorealchemy.coremod.MethodPatcher;
import targoss.hardcorealchemy.coremod.ObfuscatedName;

public class TDecayingCrops extends MethodPatcher {
    private static final String STEM_CROP = "net.minecraft.block.BlockStem";
    private static final String PAM_CROP = "com.pam.harvestcraft.blocks.growables.BlockPamCrop";
    private static final String MYSICAL_CROP = "com.blakebr0.mysticalagriculture.blocks.crop.BlockMysticalCrop";
    private static final ObfuscatedName UPDATE_TICK = new ObfuscatedName("updateTick", "func_180650_b");

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.equals(STEM_CROP) || transformedName.equals(PAM_CROP) || transformedName.equals(MYSICAL_CROP)) {
            return transformClass(transformedName, basicClass, ClassWriter.COMPUTE_MAXS);
        }
        return basicClass;
    }
    
    @Override
    public void transformMethod(MethodNode method) {
        if (method.name.equals(UPDATE_TICK.get())) {
            InsnList instructions = method.instructions;
            ListIterator<AbstractInsnNode> iterator = instructions.iterator();
            while (iterator.hasNext()) {
                AbstractInsnNode insn = iterator.next();
                if (insn.getOpcode() == Opcodes.RETURN) {
                    InsnList hook = new InsnList();
                    hook.add(new VarInsnNode(Opcodes.ALOAD, 3)); //IBlockState
                    hook.add(new VarInsnNode(Opcodes.ALOAD, 1)); //World
                    hook.add(new VarInsnNode(Opcodes.ALOAD, 2)); //BlockPos
                    hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                            "targoss/hardcorealchemy/listener/ListenerCrops",
                            "checkDecay",
                            "(Lnet/minecraft/block/state/IBlockState;" +
                            "Lnet/minecraft/world/World;" +
                            "Lnet/minecraft/util/math/BlockPos;"+
                            ")V",
                            false
                            ));
                    
                    instructions.insertBefore(insn, hook);
                }
            }
        }
    }
}
