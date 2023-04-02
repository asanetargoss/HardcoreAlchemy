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

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;

import targoss.hardcorealchemy.coremod.MethodPatcher;

/**
 * Prevent villagers from VillageBox from reviving.
 * This is so players in hostile permanent morphs can't
 * kill the same villager over and over.
 */
public class TDataVillage extends MethodPatcher {
    private static final String DATA_VILLAGE = "ckhbox.villagebox.common.village.data.DataVillage";
    private static final String ADD_DEAD_VILLAGER = "addDeadVillager";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.equals(DATA_VILLAGE)) {
            return transformClass(transformedName, basicClass, 0);
        }
        return basicClass;
    }

    @Override
    public void transformMethod(MethodNode method) {
        if (method.name.equals(ADD_DEAD_VILLAGER)) {
            InsnList hook = new InsnList();
            hook.add(new InsnNode(Opcodes.RETURN));
            hook.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
            method.instructions.insert(hook);
        }
    }

}
