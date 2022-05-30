/*
 * Copyright 2017-2022 asanetargoss
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

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import targoss.hardcorealchemy.coremod.MethodPatcher;
import targoss.hardcorealchemy.coremod.ObfuscatedName;

public class TRenderItem extends MethodPatcher {
    private static final String RENDER_ITEM = "net.minecraft.client.renderer.RenderItem";
    private final ObfuscatedName RENDER_ITEM_FUNC = new ObfuscatedName("func_181564_a" /*renderItem(ItemStack, ItemCameraTransforms.TransformType)*/);
    
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.equals(RENDER_ITEM)) {
            return transformClass(transformedName, basicClass, 0);
        }
        return basicClass;
    }

    @Override
    public void transformMethod(MethodNode method) {
        if (method.name.equals(RENDER_ITEM_FUNC.get())) {
            // Override the value of the ItemStack being rendered in-world via event hook.
            // In the future, it may be valuable to store the ItemCameraTransforms.TransformType in the event,
            // but for now we'll just assume this is only used for in-world rendering.
            InsnList patch = new InsnList();
            patch.add(new VarInsnNode(Opcodes.ALOAD, 1)); // itemStack
            patch.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                    "targoss/hardcorealchemy/event/EventDrawWorldItem",
                    "onDrawItem",
                    "(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;",
                    false));
            patch.add(new VarInsnNode(Opcodes.ASTORE, 1));
            method.instructions.insert(patch);
        }
    }

}
