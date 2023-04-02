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
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import targoss.hardcorealchemy.coremod.MethodPatcher;

public class TEntityExtension extends MethodPatcher {
    protected static final String ENTITY_EXTENSION = "am2.extensions.EntityExtension";
    protected static final String MANA_BURNOUT_TICK = "manaBurnoutTick";
    protected static final String GET_CURRENT_MANA = "getCurrentMana";
    protected static final String SET_CURRENT_MANA = "setCurrentMana";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.equals(ENTITY_EXTENSION)) {
            return transformClass(transformedName, basicClass, ClassWriter.COMPUTE_MAXS);
        }
        return basicClass;
    }

    @Override
    public void transformMethod(MethodNode method) {
        if (method.name.equals(MANA_BURNOUT_TICK)) {
            InsnList insns = method.instructions;
            ListIterator<AbstractInsnNode> iter = insns.iterator();
            int state = 0;
            // Look for setCurrentMana(getCurrentMana() + manaToAdd);
            // and patch just before setCurrentMana is invoked, so that the new behavior is:
            // setCurrentMana(EventRegenMana.onRegenMana(getCurrentMana() + manaToAdd, getCurrentMana(), this.entity));
            while (iter.hasNext()) {
                AbstractInsnNode insn = iter.next();
                switch (state) {
                case 0:
                    if (insn.getOpcode() == Opcodes.INVOKEVIRTUAL && ((MethodInsnNode)insn).name.equals(GET_CURRENT_MANA)) {
                        ++state;
                    }
                    break;
                case 1:
                    if (insn.getOpcode() == Opcodes.FLOAD) {
                        ++state;
                    } else {
                        state = 0;
                    }
                    break;
                case 2:
                    if (insn.getOpcode() == Opcodes.FADD) {
                        ++state;
                    } else {
                        state = 0;
                    }
                    break;
                case 3:
                    if (insn.getOpcode() == Opcodes.INVOKEVIRTUAL && ((MethodInsnNode)insn).name.equals(SET_CURRENT_MANA)) {
                        InsnList patch = new InsnList();
                        patch.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        patch.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
                                ENTITY_EXTENSION.replace('.', '/'),
                                GET_CURRENT_MANA,
                                "()F",
                                false));
                        patch.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        patch.add(new FieldInsnNode(Opcodes.GETFIELD,
                                ENTITY_EXTENSION.replace('.', '/'),
                                "entity",
                                "Lnet/minecraft/entity/EntityLivingBase;"));
                        patch.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                                "targoss/hardcorealchemy/magic/event/EventRegenMana",
                                "onRegenMana",
                                "(FFLnet/minecraft/entity/EntityLivingBase;)F",
                                false));
                        insns.insertBefore(insn, patch);
                        state = 0;
                    }
                    break;
                }
            }
        }
    }

}
