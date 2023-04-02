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

package targoss.hardcorealchemy.capability;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import targoss.hardcorealchemy.util.Serialization;

/**
 * A virtual capability is a capability object which is attached
 * to a capability provider, but is not serialized through the
 * normal means, allowing the capability data to instead be serialized
 * elsewhere.
 * 
 * This is currently used for attaching data to items in a way that
 * more easily syncs to the client, since Forge capabilities do not
 * sync, but without serializing the data twice.
 */
public class VirtualCapabilityManager {
    public static VirtualCapabilityManager INSTANCE = new VirtualCapabilityManager();
    
    @SuppressWarnings("rawtypes")
    protected final Map<String, Capability> capabilityRegistry = new HashMap<>();
    @SuppressWarnings("rawtypes")
    protected final Map<Capability, String> capabilityRegistryReverse = new HashMap<>();
    
    /**
     * Required to use virtual capabilities, which are a way to "store" capabilities in
     * the ItemStack nbt which normally gets synced by Minecraft to the client.
     * You must also register the capability in the normal way.
     * 
     * We don't really care about sidedness and just work with the capability instances directly.
     */
    public <T> void registerVirtualCapability(ResourceLocation key, Capability<T> capability) {
        if (capability == null) {
            throw new NullPointerException("Attempted to register null capability");
        }
        String keyS = key.toString();
        if (!capabilityRegistry.containsKey(keyS)) {
            capabilityRegistry.put(keyS, capability);
            capabilityRegistryReverse.put(capability, keyS);
        }
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected VirtualCapTrackingTag getVirtuals(ItemStack stack, boolean create) {
        NBTTagCompound maybeVirtuals = stack.getSubCompound(VirtualCapTrackingTag.KEY, create);
        if (!create && maybeVirtuals == null) {
            return null;
        }
        if (maybeVirtuals instanceof VirtualCapTrackingTag) {
            return (VirtualCapTrackingTag)maybeVirtuals;
        }
        VirtualCapTrackingTag virtuals = new VirtualCapTrackingTag();
        virtuals.merge((NBTTagCompound)maybeVirtuals);
        // When virtuals is created, deserialize all capabilities found
        for (String key : virtuals.getKeySet()) {
            Capability cap = capabilityRegistry.get(key);
            if (cap == null) {
                continue;
            }
            if (!virtuals.hasKey(key, Serialization.NBT_COMPOUND_ID)) {
                continue;
            }
            NBTTagCompound capNbt = virtuals.getCompoundTag(key);
            Object instance = cap.getDefaultInstance();
            cap.readNBT(instance, null, capNbt);
            ICapabilityProvider prov = new SimpleProvider(cap, instance);
            virtuals.addProvider(prov);
        }
        stack.setTagInfo(VirtualCapTrackingTag.KEY, virtuals);
        return virtuals;
    }
    
    public <T> boolean hasVirtualCapability(ItemStack itemStack, Capability<T> capability) {
        VirtualCapTrackingTag virtuals = getVirtuals(itemStack, false);
        if (virtuals == null) {
            return false;
        }
        return virtuals.hasCapability(capability, null);
    }
    
    /**
     * Fetch a capability as nbt in the ItemStack's stackTagCompound using
     * the capability resource location as a key. Note that the capability
     * instance you receive will not be serialized automatically. Call
     * updateVirtualCapability with your instance once it is in the
     * desired state.
     * 
     * Before using this, call registerVirtualCapability
     */
    public <T> T getVirtualCapability(ItemStack itemStack, Capability<T> capability, boolean create) {
        VirtualCapTrackingTag virtuals = getVirtuals(itemStack, create);
        if (virtuals == null) {
            return null;
        }
        T instance = virtuals.getCapability(capability, null);
        if (instance == null) {
            if (!create) {
                return null;
            }
            instance = capability.getDefaultInstance();
            ICapabilityProvider prov = new SimpleProvider<T>(capability, instance);
            virtuals.addProvider(prov);
        }
        return instance;
    }

    /**
     * Add or replace the capability attached to the item. Note that the
     * capability instance you set will not be serialized automatically.
     * Call updateVirtualCapability with your instance once it is in the
     * desired state.
     * 
     * Before using this, call registerVirtualCapability
     */
    public <T> void setVirtualCapability(ItemStack itemStack, Capability<T> capability, T instance) {
        VirtualCapTrackingTag virtuals = getVirtuals(itemStack, true);
        ICapabilityProvider prov = new SimpleProvider<T>(capability, instance);
        virtuals.addOrReplaceProvider(capability, prov);
    }
    
    /**
     * Store a capability as nbt in the ItemStack's stackTagCompound by utilizing
     * the serialization features of capability implementations. The capability
     * is serialized immediately upon function call and only once.
     * 
     * Before using this, call registerVirtualCapability
     */
    public <T> void updateVirtualCapability(ItemStack itemStack, Capability<T> capability) {
        String key = capabilityRegistryReverse.get(capability);
        if (key == null) {
            return;
        }
        VirtualCapTrackingTag virtuals = getVirtuals(itemStack, true);
        T instance = virtuals.getCapability(capability, null);
        if (instance == null) {
            throw new IllegalStateException("Nothing to serialize as the capability is not set: " + capability);
        }
        NBTBase thisCapNBT = capability.getStorage().writeNBT(capability, instance, null);
        virtuals.setTag(key, thisCapNBT);
    }
}