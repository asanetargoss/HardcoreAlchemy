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

package targoss.hardcorealchemy.capability.cache;

import net.minecraft.util.DamageSource;

/**
 * For stuff that doesn't get synced to the client version of an entity
 * Not stored.
 */
public interface IClientEntityCache {
    /** Called only when a player attacks an entity on the client */
    void setDamageSource(DamageSource source);
    DamageSource getDamageSource();
    void setLastDamageSourceUpdate(long time);
    long getLastDamageSourceUpdate();
}
