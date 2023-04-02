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

package targoss.hardcorealchemy.magic.will;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import targoss.hardcorealchemy.ModState;
import thaumcraft.common.world.aura.AuraHandler;

public class WillState {
    public static float willFromThaumcraftAura(float thaumcraftAura) {
        // 250 is the average initial amount of Thaumcraft vis in the middle of a standard forest biome
        return thaumcraftAura / 250.0F;
    }
    
    public static boolean isTypicalMagicCastingEnvironment(World world, BlockPos pos) {
        boolean isOverworld = world.provider.getDimensionType() == DimensionType.OVERWORLD;
        return isOverworld;
    }
    
    /**
     * Returns the amount of Will of a given type present at a location.
     * Values typically range between 0 and 1, but can often be greater than 1
     * for especially "magical" areas.
     * The current system is rather limited and only measures the overworld.
     */
    public static float getWillAmount(WillType type, World world, BlockPos pos) {
        boolean isOverworld = world.provider.getDimensionType() == DimensionType.OVERWORLD;
        if (!isOverworld) {
            return 0.0F;
        }
        if (ModState.isThaumcraftLoaded) {
            if (type == Wills.AURA_AIR) {
                return willFromThaumcraftAura(AuraHandler.getVis(world, pos));
            } else if (type == Wills.SCOURGE_AIR) {
                return willFromThaumcraftAura(AuraHandler.getFlux(world, pos));
            } else {
                return 0.0F;
            }
        } else {
            if (type == Wills.AURA_AIR) {
                return 1.0F;
            } else {
                return 0.0F;
            }
        }
    }
}
