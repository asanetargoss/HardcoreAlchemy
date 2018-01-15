/*
 * Copyright 2017-2018 asanetargoss
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

package targoss.hardcorealchemy.capability;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class CapUtil {
    /**
     * Copy pretty much any capability by taking advantage of NBT serialization
     * Returns true if successful
     */
    public static <T,V extends ICapabilityProvider,W extends ICapabilityProvider> boolean
            copyOldToNew(Capability<T> capability, V oldProvider, W newProvider) {
        T oldCapability = oldProvider.getCapability(capability, null);
        if (oldCapability == null) {
            return false;
        }
        T newCapability = newProvider.getCapability(capability, null);
        if (newCapability == null) {
            return false;
        }
        IStorage<T> storage = capability.getStorage();
        NBTBase oldNBT = storage.writeNBT(capability, oldCapability, null);
        storage.readNBT(capability, newCapability, null, oldNBT);
        return true;
    }
    
    private static final Map<Capability, ResourceLocation> capabilityRegistry = new HashMap<Capability, ResourceLocation>();
    
    /**
     * Required to use virtual capabilities, which are a way to "store" capabilities in
     * the ItemStack nbt which normally gets synced by Minecraft to the client.
     * You must also register the capability in the normal way.
     * 
     * We don't really care about sidedness and just work with the capability instances directly.
     */
    public static void registerVirtualCapability(ResourceLocation key, Capability capability) {
        if (!capabilityRegistry.containsKey(capability)) {
            capabilityRegistry.put(capability, key);
        }
    }
    
    public static <T> boolean hasVirtualCapability(ItemStack itemStack, Capability<T> capability) {
        ResourceLocation key = capabilityRegistry.get(capability);
        if (key == null) {
            return false;
        }
        NBTTagCompound capNbt = itemStack.getSubCompound("VirtualCaps", true);
        NBTTagCompound thisCapNBT = capNbt.getCompoundTag(key.toString());
        return thisCapNBT != null;
    }
    
    /**
     * Fetch a capability as nbt in the ItemStack's stackTagCompound using
     * the capability resource location as a key. Note that the capability
     * instance you receive will not be serialized automatically. Call
     * setVirtualCapability with your instance once it is in the
     * desired state.
     * 
     * Before using this, call registerVirtualCapability
     */
    public static <T> T getVirtualCapability(ItemStack itemStack, Capability<T> capability) {
        ResourceLocation key = capabilityRegistry.get(capability);
        if (key == null) {
            return null;
        }
        
        NBTTagCompound capNbt = itemStack.getSubCompound("VirtualCaps", false);
        if (capNbt == null) {
            return null;
        }
        
        NBTTagCompound thisCapNBT = capNbt.getCompoundTag(key.toString());
        if (thisCapNBT == null) {
            return null;
        }
        
        T instance = capability.getDefaultInstance();
        capability.getStorage().readNBT(capability, instance, null, thisCapNBT);
        return instance;
    }
    
    /**
     * Store a capability as nbt in the ItemStack's stackTagCompound by utilizing
     * the serialization features of capability implementations. The capability
     * is serialized immediately upon function call and only once.
     * 
     * Before using this, call registerVirtualCapability
     */
    public static <T> void setVirtualCapability(ItemStack itemStack, Capability<T> capability, T instance) {
        ResourceLocation key = capabilityRegistry.get(capability);
        if (key == null) {
            return;
        }
        
        NBTTagCompound capNbt = itemStack.getSubCompound("VirtualCaps", true);
        capNbt.setTag(key.toString(), capability.getStorage().writeNBT(capability, instance, null));
    }
}
