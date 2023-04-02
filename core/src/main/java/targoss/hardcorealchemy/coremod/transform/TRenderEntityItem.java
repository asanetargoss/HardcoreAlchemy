/*
 * Copyright 2017-2023 asanetargoss
 *
 * This file is part of Hardcore Alchemy Core.
 *
 * Hardcore Alchemy Core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Core is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Core. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.coremod.transform;

import java.util.ListIterator;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import targoss.hardcorealchemy.coremod.MethodPatcher;
import targoss.hardcorealchemy.coremod.ObfuscatedName;

public class TRenderEntityItem extends MethodPatcher {
    private static final String RENDER_ENTITY_ITEM = "net.minecraft.client.renderer.entity.RenderEntityItem";
    private static final ObfuscatedName DO_RENDER = new ObfuscatedName("func_76986_a" /*doRender*/);
    private static final ObfuscatedName GET_ENTITY_ITEM = new ObfuscatedName("func_92059_d" /*EntityItem.getEntityItem()*/);

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.equals(RENDER_ENTITY_ITEM)) {
            return transformClass(transformedName, basicClass, 0);
        }
        return basicClass;
    }

    @Override
    public void transformMethod(MethodNode method) {
        if (method.name.equals(DO_RENDER.get())) {
            InsnList instructions = method.instructions;
            ListIterator<AbstractInsnNode> iterator = instructions.iterator();
            
            while (iterator.hasNext()) {
                AbstractInsnNode hook = iterator.next();
                
                if (hook.getOpcode() == Opcodes.INVOKEVIRTUAL &&
                        ((MethodInsnNode)hook).name.equals(GET_ENTITY_ITEM.get())) {
                    InsnList patch = new InsnList();
                    patch.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                            "targoss/hardcorealchemy/event/EventDrawWorldItem",
                            "onDrawItem",
                            "(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;",
                            false));
                    instructions.insert(hook, patch);
                    break;
                }
            }
        }
    }

}
