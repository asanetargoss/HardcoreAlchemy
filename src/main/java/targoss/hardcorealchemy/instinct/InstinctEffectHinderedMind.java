/*
 * Copyright 2019 asanetargoss
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

package targoss.hardcorealchemy.instinct;

import net.minecraft.entity.player.EntityPlayer;
import targoss.hardcorealchemy.instinct.api.InstinctEffect;

/**
 * Most behavior is handled through ListenerPlayerHinderedMind
 */
public class InstinctEffectHinderedMind extends InstinctEffect {
    @Override
    public void onActivate(EntityPlayer player, float amplifier) {}

    @Override
    public void onDeactivate(EntityPlayer player, float amplifier) {}

    @Override
    public void tick(EntityPlayer player, float amplifier) {}
}
