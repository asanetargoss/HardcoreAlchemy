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

package targoss.hardcorealchemy.util;

import am2.container.slot.SlotMagiciansWorkbenchCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraftforge.fml.common.Optional;
import targoss.hardcorealchemy.ModState;
import thaumcraft.common.container.slot.SlotCraftingArcaneWorkbench;

public class InventoryUtil {
    public static boolean isCraftingSlot(Slot slot) {
        if (slot instanceof SlotCrafting) {
            return true;
        }
        if (ModState.isThaumcraftLoaded && isThaumcraftCraftingSlot(slot)) {
            return true;
        }
        if (ModState.isArsMagicaLoaded && isArsMagicaCraftingSlot(slot)) {
            return true;
        }
        
        return false;
    }

    @Optional.Method(modid=ModState.THAUMCRAFT_ID)
    private static boolean isThaumcraftCraftingSlot(Slot slot) {
        return slot instanceof SlotCraftingArcaneWorkbench;
    }
    
    @Optional.Method(modid=ModState.ARS_MAGICA_ID)
    private static boolean isArsMagicaCraftingSlot(Slot slot) {
        return slot instanceof SlotMagiciansWorkbenchCrafting;
    }
}
