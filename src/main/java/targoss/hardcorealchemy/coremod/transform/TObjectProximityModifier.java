/**
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
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import targoss.hardcorealchemy.coremod.MethodPatcher;

public class TObjectProximityModifier extends MethodPatcher {
    public static final String OBJECT_PROXIMITY_MODIFIER = "toughasnails.temperature.modifier.ObjectProximityModifier";
    public static final String GET_BLOCK_TEMPERATURE = "getBlockTemperature";
    public static final String GET_REGISTRY_NAME = "getRegistryName";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.equals(OBJECT_PROXIMITY_MODIFIER)) {
            return transformClass(transformedName, basicClass, ClassWriter.COMPUTE_MAXS + ClassWriter.COMPUTE_FRAMES);
        }
        return basicClass;
    }

    @Override
    public void transformMethod(MethodNode method) {
        if (method.name.equals(GET_BLOCK_TEMPERATURE)) {
            InsnList instructions = method.instructions;
            ListIterator<AbstractInsnNode> iterator = instructions.iterator();
            while (iterator.hasNext()) {
                AbstractInsnNode insn = iterator.next();
                if (insn.getOpcode() == Opcodes.INVOKEVIRTUAL &&
                        ((MethodInsnNode)insn).name.equals(GET_REGISTRY_NAME)) {
                    InsnList patch = new InsnList();
                    patch.add(new InsnNode(Opcodes.DUP)); // Copy of ResourceLocation (return value of getRegistryName)
                    LabelNode resume = new LabelNode();
                    patch.add(new JumpInsnNode(Opcodes.IFNONNULL, resume)); // Null check; skip return if OK
                    patch.add(new InsnNode(Opcodes.FCONST_0));
                    patch.add(new InsnNode(Opcodes.FRETURN)); // We're null; don't progress further!
                    patch.add(resume); // We're okay; no NPE risk
                    
                    instructions.insert(insn, patch);
                }
                
            }
        }
    }

}
