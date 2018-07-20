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
import net.minecraft.util.ResourceLocation;
import targoss.hardcorealchemy.HardcoreAlchemy;

public class ClientEntityCache implements IClientEntityCache {
    public static final ResourceLocation RESOURCE_LOCATION = new ResourceLocation(HardcoreAlchemy.MOD_ID, "client_entity_cache");
    
    private DamageSource damageSource = null;
    private long lastDamageSourceUpdate = 0;

    @Override
    public void setDamageSource(DamageSource source) {
        this.damageSource = source;
    }

    @Override
    public DamageSource getDamageSource() {
        return damageSource;
    }

    @Override
    public void setLastDamageSourceUpdate(long time) {
        this.lastDamageSourceUpdate = time;
    }

    @Override
    public long getLastDamageSourceUpdate() {
        return lastDamageSourceUpdate;
    }
}
