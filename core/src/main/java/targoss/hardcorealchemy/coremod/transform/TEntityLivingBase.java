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

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import targoss.hardcorealchemy.coremod.MethodPatcher;
import targoss.hardcorealchemy.coremod.ObfuscatedName;

public class TEntityLivingBase extends MethodPatcher {
    private static final String ENTITY_LIVING_BASE = "net.minecraft.entity.EntityLivingBase";
	private static final ObfuscatedName ENTITY_DAMAGE = new ObfuscatedName("func_70097_a" /*attackEntityFrom*/);
	
	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if (transformedName.equals(ENTITY_LIVING_BASE)) {
		    return transformClass(transformedName, basicClass, 0);
		}
		return basicClass;
	}

    @Override
    public void transformMethod(MethodNode method) {
        if (method.name.equals(ENTITY_DAMAGE.get())) {
            InsnList instructions = method.instructions;
            
            InsnList eventHook = new InsnList();
            eventHook.add(new VarInsnNode(Opcodes.ALOAD, 0));
            eventHook.add(new VarInsnNode(Opcodes.ALOAD, 1));
            eventHook.add(new VarInsnNode(Opcodes.FLOAD, 2));
            eventHook.add(new MethodInsnNode(
                    Opcodes.INVOKESTATIC, "targoss/hardcorealchemy/event/EventLivingAttack",
                    "onLivingAttack", "(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/util/DamageSource;F)F", false
                    ));
            eventHook.add(new VarInsnNode(Opcodes.FSTORE, 2));
            
            instructions.insert(eventHook);
        }
    }
	
}
