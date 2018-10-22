/*
 * Copyright 2018 asanetargoss
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

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import targoss.hardcorealchemy.coremod.MethodPatcher;

/**
 * Makes ClassInheritanceMultiMap thread-safe
 * by replacing its map/set objects with thread-safe
 * counterparts.
 */
public class TClassInheritanceMultiMap extends MethodPatcher {
    private static final String CLASS_INHERITANCE_MULTI_MAP = "net.minecraft.util.ClassInheritanceMultiMap";
    
    // TODO: Remove after testing
    @Override public boolean enableDebug() { return true; }
    
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.equals(CLASS_INHERITANCE_MULTI_MAP)) {
            return transformClass(transformedName, basicClass, 0);
        }
        return basicClass;
    }

    @Override
    public void transformMethod(MethodNode method) {
        if (method.name.equals("<clinit>")) {
            InsnList instructions = method.instructions;
            ListIterator<AbstractInsnNode> iterator = instructions.iterator();
            while (iterator.hasNext()) {
                AbstractInsnNode insn = iterator.next();
                if (insn.getOpcode() == Opcodes.INVOKESTATIC) {
                    MethodInsnNode methodInsn = (MethodInsnNode)insn;
                    if (methodInsn.name.equals("newHashSet")) {
                        methodInsn.name = "newConcurrentHashSet";
                        methodInsn.desc = "()Ljava/util/Set;";
                    }
                }
            }
        }
        else if (method.name.equals("<init>")) {
            InsnList instructions = method.instructions;
            ListIterator<AbstractInsnNode> iterator = instructions.iterator();
            while (iterator.hasNext()) {
                AbstractInsnNode insn = iterator.next();
                if (insn.getOpcode() == Opcodes.INVOKESTATIC) {
                    MethodInsnNode methodInsn = (MethodInsnNode)insn;
                    if (methodInsn.name.equals("newHashMap")) {
                        methodInsn.name = "newConcurrentMap";
                        methodInsn.desc = "()Ljava/util/concurrent/ConcurrentMap;";
                    }
                    else if (methodInsn.name.equals("newIdentityHashSet")) {
                        instructions.insert(methodInsn, new MethodInsnNode(Opcodes.INVOKESTATIC,
                                "java/util/Collections",
                                "synchronizedSet",
                                "(Ljava/util/Set;)Ljava/util/Set;",
                                false));
                        iterator.next();
                    }
                }
                
            }
        }
    }

}
