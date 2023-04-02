/*
 * Copyright 2017-2023 asanetargoss
 *
 * This file is part of Hardcore Alchemy Magic.
 *
 * Hardcore Alchemy Magic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Magic is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Magic. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.magic.coremod.transform;

import java.util.ListIterator;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import targoss.hardcorealchemy.coremod.MethodPatcher;

public class TThaumcraftPlayerEvents extends MethodPatcher {
    private static final String PLAYER_EVENTS = "thaumcraft.common.lib.events.PlayerEvents";
    private static final String PICKUP_ITEM = "pickupItem";
    private static final String IS_RESEARCH_KNOWN = "isResearchKnown";
    private static final String CRYSTAL_RESEARCH = "!gotcrystals";
    private static final String BOOK_RESEARCH = "!gotthaumonomicon";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.equals(PLAYER_EVENTS)) {
            return transformClass(transformedName, basicClass, ClassWriter.COMPUTE_MAXS);
        }
        return basicClass;
    }

    @Override
    public void transformMethod(MethodNode method) {
        if (method.name.equals(PICKUP_ITEM)) {
            InsnList instructions = method.instructions;
            ListIterator<AbstractInsnNode> iterator = instructions.iterator();
            String lastLoadedString = "";
            boolean foundCrystalResearchCheck = false;
            boolean foundBookResearchCheck = false;
            boolean patchedCrystalResearchCheck = false;
            boolean patchedBookResearchCheck = false;
            
            while (iterator.hasNext() && !(patchedCrystalResearchCheck && patchedBookResearchCheck)) {
                AbstractInsnNode insn = iterator.next();
                
                switch (insn.getOpcode()) {
                case Opcodes.LDC:
                    LdcInsnNode loadInsn = (LdcInsnNode)insn;
                    if (loadInsn.cst instanceof String) {
                        lastLoadedString = (String)loadInsn.cst;
                    }
                break;
                case Opcodes.INVOKEINTERFACE:
                    MethodInsnNode methodInsn = (MethodInsnNode)insn;
                    if (methodInsn.name.equals(IS_RESEARCH_KNOWN) &&
                            lastLoadedString.equals(CRYSTAL_RESEARCH)) {
                        foundCrystalResearchCheck = true;
                    }
                    else if (methodInsn.name.equals(IS_RESEARCH_KNOWN) &&
                            lastLoadedString.equals(BOOK_RESEARCH)) {
                        foundBookResearchCheck = true;
                    }
                break;
                case Opcodes.IFNE:
                    if (foundCrystalResearchCheck) {
                        InsnList patch = new InsnList();
                        // Get EntityItemPickupEvent
                        patch.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        // EntityItemPickupEvent.getEntityPlayer() (EntityPlayer)
                        patch.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
                                "net/minecraftforge/event/entity/player/EntityItemPickupEvent",
                                "getEntityPlayer",
                                "()Lnet/minecraft/entity/player/EntityPlayer;",
                                false));
                        // ListenerPlayerMagicState.canStartThaumcraftResearch(EntityItemPickupEvent) (boolean)
                        patch.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                                "targoss/hardcorealchemy/magic/listener/ListenerPlayerMagicState",
                                "canStartThaumcraftResearch",
                                "(Lnet/minecraft/entity/player/EntityPlayer;)Z",
                                false));
                        // If false, skip the crystal research
                        patch.add(new JumpInsnNode(Opcodes.IFEQ, ((JumpInsnNode)insn).label));
                        
                        instructions.insert(insn, patch);
                        patchedCrystalResearchCheck = true;
                        foundCrystalResearchCheck = false;
                    }
                    else if (foundBookResearchCheck) {
                        InsnList patch = new InsnList();
                        // Get EntityItemPickupEvent
                        patch.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        // EntityItemPickupEvent.getEntityPlayer() (EntityPlayer)
                        patch.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
                                "net/minecraftforge/event/entity/player/EntityItemPickupEvent",
                                "getEntityPlayer",
                                "()Lnet/minecraft/entity/player/EntityPlayer;",
                                false));
                        // ListenerPlayerMagicState.canThaumonomiconPickupUnlockResearch(EntityItemPickupEvent) (boolean)
                        patch.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                                "targoss/hardcorealchemy/magic/listener/ListenerPlayerMagicState",
                                "canThaumonomiconPickupUnlockResearch",
                                "(Lnet/minecraft/entity/player/EntityPlayer;)Z",
                                false));
                        // If false, skip the crystal research
                        patch.add(new JumpInsnNode(Opcodes.IFEQ, ((JumpInsnNode)insn).label));
                        
                        instructions.insert(insn, patch);
                        patchedBookResearchCheck = true;
                        foundBookResearchCheck = false;
                    }
                break;
                default:
                break;
                }
            }
            
        }
    }

}
