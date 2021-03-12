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

package targoss.hardcorealchemy.capability.misc;

import java.util.UUID;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class StorageMisc implements IStorage<ICapabilityMisc> {
    private static final String HAS_SEEN_THIRST_WARNING = "has_seen_thirst_warning";
    private static final String HAS_SEEN_MAGIC_INHIBITION_WARNING = "has_seen_magic_inhibition_warning";
    private static final String LIFETIME_UUID = "lifetimeUUID";
    private static final String LAST_LOGIN_VERSION = "lastLoginVersion";
    @Override
    public NBTBase writeNBT(Capability<ICapabilityMisc> capability, ICapabilityMisc instance, EnumFacing side) {
        NBTTagCompound nbt = new NBTTagCompound();
        
        nbt.setBoolean(HAS_SEEN_THIRST_WARNING, instance.getHasSeenThirstWarning());
        nbt.setBoolean(HAS_SEEN_MAGIC_INHIBITION_WARNING, instance.getHasSeenMagicInhibitionWarning());
        
        {
            UUID uuid = instance.getLifetimeUUID();
            if (uuid != null) {
                nbt.setString(LIFETIME_UUID, uuid.toString());
            }
        }
        
        if (instance.getLastLoginVersion() != null) {
            nbt.setString(LAST_LOGIN_VERSION, instance.getLastLoginVersion());
        }
        
        return nbt;
    }

    @Override
    public void readNBT(Capability<ICapabilityMisc> capability, ICapabilityMisc instance, EnumFacing side, NBTBase nbtBase) {
        if (!(nbtBase instanceof NBTTagCompound)) {
            return;
        }
        NBTTagCompound nbt = (NBTTagCompound)nbtBase;
        
        instance.setHasSeenThirstWarning(nbt.getBoolean(HAS_SEEN_THIRST_WARNING));
        instance.setHasSeenMagicInhibitionWarning(nbt.getBoolean(HAS_SEEN_MAGIC_INHIBITION_WARNING));
        
        if (nbt.hasKey(LIFETIME_UUID)) {
            try {
                UUID uuid = UUID.fromString(nbt.getString(LIFETIME_UUID));
                instance.setLifetimeUUID(uuid);
            } catch (IllegalArgumentException e) {}
        }
        
        if (nbt.hasKey(LAST_LOGIN_VERSION)) {
            instance.setLastLoginVersion(nbt.getString(LAST_LOGIN_VERSION));
        }
    }
}
