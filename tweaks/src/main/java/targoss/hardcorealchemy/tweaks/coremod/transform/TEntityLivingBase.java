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

package targoss.hardcorealchemy.tweaks.coremod.transform;

import java.util.ListIterator;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import targoss.hardcorealchemy.coremod.MethodPatcher;
import targoss.hardcorealchemy.coremod.ObfuscatedName;

public class TEntityLivingBase extends MethodPatcher {
    private static final String ENTITY_LIVING_BASE = "net.minecraft.entity.EntityLivingBase";
	private static final ObfuscatedName ATTACK_ENTITY_FROM = new ObfuscatedName("func_70097_a" /*attackEntityFrom*/);
	
	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if (transformedName.equals(ENTITY_LIVING_BASE)) {
		    return transformClass(transformedName, basicClass, 0);
		}
		return basicClass;
	}

    @Override
    public void transformMethod(MethodNode method) {
        if (method.name.equals(ATTACK_ENTITY_FROM.get())) {
            InsnList instructions = method.instructions;
            
            {
                ListIterator<AbstractInsnNode> it = instructions.iterator();
                while (it.hasNext()) {
                    AbstractInsnNode insn = it.next();
                    if (insn.getOpcode() == Opcodes.IRETURN) {
                        InsnList hook = new InsnList();
                        hook.add(new InsnNode(Opcodes.DUP));
                        hook.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        hook.add(new VarInsnNode(Opcodes.ALOAD, 1));
                        hook.add(new VarInsnNode(Opcodes.FLOAD, 2));
                        hook.add(new MethodInsnNode(
                                Opcodes.INVOKESTATIC, "targoss/hardcorealchemy/tweaks/event/EventLivingAttack",
                                "onLivingAttackEnd", "(ZLnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/util/DamageSource;F)V", false
                                ));
                        
                        instructions.insertBefore(insn, hook);
                    }
                }
            }
            {
                InsnList hook = new InsnList();
                hook.add(new VarInsnNode(Opcodes.ALOAD, 0));
                hook.add(new VarInsnNode(Opcodes.ALOAD, 1));
                hook.add(new VarInsnNode(Opcodes.FLOAD, 2));
                hook.add(new MethodInsnNode(
                        Opcodes.INVOKESTATIC, "targoss/hardcorealchemy/tweaks/event/EventLivingAttack",
                        "onLivingAttackStart", "(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/util/DamageSource;F)F", false
                        ));
                hook.add(new VarInsnNode(Opcodes.FSTORE, 2));
                
                instructions.insert(hook);
            }
        }
    }
	
}
