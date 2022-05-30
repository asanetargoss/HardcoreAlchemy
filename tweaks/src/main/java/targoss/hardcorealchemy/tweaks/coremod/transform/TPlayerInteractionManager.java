/*
 * Copyright 2017-2022 asanetargoss
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
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import targoss.hardcorealchemy.coremod.MethodPatcher;
import targoss.hardcorealchemy.coremod.ObfuscatedName;

public class TPlayerInteractionManager extends MethodPatcher {
    protected static final String PLAYER_INTERACTION_MANAGER = "net.minecraft.server.management.PlayerInteractionManager";
    protected static final ObfuscatedName TRY_HARVEST_BLOCK = new ObfuscatedName("func_180237_b" /*tryHarvestBlock*/);
    protected static final ObfuscatedName ON_BLOCK_DESTROYED = new ObfuscatedName("func_179548_a" /*onBlockDestroyed*/);
    protected static final ObfuscatedName PLAYER = new ObfuscatedName("field_73090_b" /*PlayerInteractionManager::player*/);

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.equals(PLAYER_INTERACTION_MANAGER)) {
            return transformClass(transformedName, basicClass, 0);
        }
        return basicClass;
    }

    @Override
    public void transformMethod(MethodNode method) {
        if (method.name.equals(TRY_HARVEST_BLOCK.get())) {
            ListIterator<AbstractInsnNode> iterator = method.instructions.iterator();
            while (iterator.hasNext()) {
                AbstractInsnNode insn = iterator.next();
                if (!(insn instanceof MethodInsnNode)) {
                    continue;
                }
                String methodName = ((MethodInsnNode)insn).name;
                if (!methodName.equals(ON_BLOCK_DESTROYED.get()) && !methodName.equals(ON_BLOCK_DESTROYED.get())) {
                    continue;
                }
                InsnList patch = new InsnList();
                patch.add(new VarInsnNode(Opcodes.ALOAD, 0));
                patch.add(new FieldInsnNode(Opcodes.GETFIELD,
                        PLAYER_INTERACTION_MANAGER.replace('.', '/'),
                        PLAYER.get(),
                        "Lnet/minecraft/entity/player/EntityPlayerMP;")); // this.player
                patch.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                        "targoss/hardcorealchemy/tweaks/event/EventItemUseResult",
                        "onPlayerBlockDestroyed",
                        "(Lnet/minecraft/entity/player/EntityPlayer;)V",
                        false));
                method.instructions.insert(insn, patch);
            }
        }
    }

}
