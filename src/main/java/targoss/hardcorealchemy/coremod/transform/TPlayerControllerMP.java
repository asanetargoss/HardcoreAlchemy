/*
 * Copyright 2020 asanetargoss
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

package targoss.hardcorealchemy.coremod.transform;

import java.util.ListIterator;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import targoss.hardcorealchemy.coremod.MethodPatcher;
import targoss.hardcorealchemy.coremod.ObfuscatedName;

public class TPlayerControllerMP extends MethodPatcher {
    protected static final String PLAYER_CONTROLLER_MP = "net.minecraft.client.multiplayer.PlayerControllerMP";
    protected static final ObfuscatedName ON_PLAYER_DAMAGE_BLOCK = new ObfuscatedName("func_180512_c" /*onPlayerDamageBlock*/);
    protected static final ObfuscatedName GET_SOUND_TYPE = new ObfuscatedName("func_185467_w" /*Block.getSoundType*/);
    protected static final ObfuscatedName GET_SOUND_TYPE_DEOBF = new ObfuscatedName("getSoundType");

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.equals(PLAYER_CONTROLLER_MP)) {
            return transformClass(transformedName, basicClass, 0);
        }
        return basicClass;
    }

    @Override
    public void transformMethod(MethodNode method) {
        if (method.name.equals(ON_PLAYER_DAMAGE_BLOCK.get())) {
            ListIterator<AbstractInsnNode> iterator = method.instructions.iterator();
            while (iterator.hasNext()) {
                AbstractInsnNode insn = iterator.next();
                if (!(insn instanceof MethodInsnNode)) {
                    continue;
                }
                String methodName = ((MethodInsnNode)insn).name;
                if (!methodName.equals(GET_SOUND_TYPE.get()) && !methodName.equals(GET_SOUND_TYPE_DEOBF.get())) {
                    continue;
                }
                InsnList patch = new InsnList();
                patch.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                        "targoss/hardcorealchemy/util/MiscVanilla",
                        "getTheMinecraftPlayer",
                        "()Lnet/minecraft/entity/player/EntityPlayer;",
                        false));
                patch.add(new VarInsnNode(Opcodes.ALOAD, 1)); // BlockPos
                patch.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                        "targoss/hardcorealchemy/event/EventPlayerDamageBlockSound",
                        "onPlayerDamageBlockSound",
                        "(Lnet/minecraft/block/SoundType;" +
                                "Lnet/minecraft/entity/player/EntityPlayer;" +
                                "Lnet/minecraft/util/math/BlockPos;)" +
                                "Lnet/minecraft/block/SoundType;",
                        false));
                method.instructions.insert(insn, patch);
            }
        }
    }

}
