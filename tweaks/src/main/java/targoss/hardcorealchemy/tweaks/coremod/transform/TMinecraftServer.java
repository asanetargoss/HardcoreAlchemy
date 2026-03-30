/*
 * Copyright 2017-2026 asanetargoss
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

import java.util.ListIterator;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.minecraft.world.EnumDifficulty;
import net.minecraftforge.common.MinecraftForge;
import targoss.hardcorealchemy.coremod.MethodPatcher;
import targoss.hardcorealchemy.coremod.ObfuscatedName;
import targoss.hardcorealchemy.tweaks.event.EventServerDifficulty;

public class TMinecraftServer extends MethodPatcher {
    protected static final String MINECRAFT_SERVER = "net.minecraft.server.MinecraftServer";
    protected static final ObfuscatedName SET_DIFFICULTY_FOR_ALL_WORLDS = new ObfuscatedName("func_147139_a" /*setDifficultyForAllWorlds*/);

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.equals(MINECRAFT_SERVER)) {
            return transformClass(transformedName, basicClass, 0);
        }
        return basicClass;
    }

    @Override
    public void transformMethod(MethodNode method) {
        if (method.name.equals(SET_DIFFICULTY_FOR_ALL_WORLDS.get())) {
            InsnList insns = method.instructions;
            ListIterator<AbstractInsnNode> iter = insns.iterator();
            while (iter.hasNext()) {
                AbstractInsnNode insn = iter.next();
                if (insn.getOpcode() == Opcodes.RETURN) {
                    InsnList hook = new InsnList();
                    hook.add(new VarInsnNode(Opcodes.ALOAD, 1)); // EnumDifficulty
                    hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                            this.getClass().getName().replace('.', '/') + "$Hooks",
                            "onSetDifficulty",
                            "(Lnet/minecraft/world/EnumDifficulty;)V",
                            false));
                    
                    method.instructions.insertBefore(insn, hook);
                }
            }
        }
    }

    public static class Hooks {
        public static void onSetDifficulty(EnumDifficulty difficulty) {
            EventServerDifficulty event = new EventServerDifficulty(difficulty);
            MinecraftForge.EVENT_BUS.post(event);
        }
    }
}
