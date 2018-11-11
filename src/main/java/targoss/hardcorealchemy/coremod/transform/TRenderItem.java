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

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import targoss.hardcorealchemy.coremod.MethodPatcher;
import targoss.hardcorealchemy.coremod.ObfuscatedName;

public class TRenderItem extends MethodPatcher {
    private static final String RENDER_ITEM = "net.minecraft.client.renderer.RenderItem";
    private static final ObfuscatedName RENDER_ITEM_OVERLAY_INTO_GUI = new ObfuscatedName("func_180453_a" /*renderItemOverlayIntoGUI*/);
    
    // TODO: Remove after testing
    @Override public boolean enableDebug() { return true; }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.equals(RENDER_ITEM)) {
            return transformClass(transformedName, basicClass, 0);
        }
        return basicClass;
    }

    @Override
    public void transformMethod(MethodNode method) {
        if (method.name.equals(RENDER_ITEM_OVERLAY_INTO_GUI.get())) {
            InsnList patch = new InsnList();
            patch.add(new VarInsnNode(Opcodes.ALOAD, 2));
            patch.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                    "targoss/hardcorealchemy/event/EventDrawItemStack",
                    "onDrawItemStack",
                    "(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;",
                    false));
            patch.add(new VarInsnNode(Opcodes.ASTORE, 2));
            method.instructions.insert(patch);
        }
    }

}
