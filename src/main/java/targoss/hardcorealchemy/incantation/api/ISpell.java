/*
 * Copyright 2020 asanetargoss
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

package targoss.hardcorealchemy.incantation.api;

import net.minecraft.entity.player.EntityPlayerMP;

public interface ISpell {
    /**
     * Return true if the player can invoke this spell.
     * For example, for spells that can be cast a limited number of times,
     * or for spells that only certain players can cast.
     */
    public boolean canInvoke(EntityPlayerMP player);

    /**
     * Have the player invoke the spell
     */
    public void invoke(EntityPlayerMP player);
}
