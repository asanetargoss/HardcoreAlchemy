package targoss.hardcorealchemy.tweaks.capability.itemcontainer;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import targoss.hardcorealchemy.util.InventoryUtil;
import targoss.hardcorealchemy.util.Serialization;

public class StorageItemContainer implements Capability.IStorage<ICapabilityItemContainer> {
    protected static final String CONTAINED_ITEM = "contained_item";
    protected static final String PROPERTY_OVERRIDES = "property_overrides";

    @Override
    public NBTBase writeNBT(Capability<ICapabilityItemContainer> capability, ICapabilityItemContainer instance,
            EnumFacing side) {
        NBTTagCompound nbt = new NBTTagCompound();

        ItemStack containedItem = instance.getContainedItem();
        if (!InventoryUtil.isEmptyItemStack(containedItem)) {
            NBTTagCompound containedItemNbt = containedItem.serializeNBT();
            nbt.setTag(CONTAINED_ITEM, containedItemNbt);
        }
        
        Map<ResourceLocation, Float> propertyOverrides = instance.getPropertyOverrides();
        if (propertyOverrides != null && !propertyOverrides.isEmpty()) {
            NBTTagCompound propertyOverridesNbt = new NBTTagCompound();
            for (Map.Entry<ResourceLocation, Float> propertyOverride : propertyOverrides.entrySet()) {
                if (propertyOverride.getValue() == null) {
                    continue;
                }
                propertyOverridesNbt.setFloat(propertyOverride.getKey().toString(), propertyOverride.getValue());
            }
            nbt.setTag(PROPERTY_OVERRIDES, propertyOverridesNbt);
        }
        
        return nbt;
    }

    @Override
    public void readNBT(Capability<ICapabilityItemContainer> capability, ICapabilityItemContainer instance, EnumFacing side,
            NBTBase nbt) {
        if (!(nbt instanceof NBTTagCompound)) {
            return;
        }
        NBTTagCompound nbtCompound = (NBTTagCompound)nbt;
        
        if (nbtCompound.hasKey(CONTAINED_ITEM, Serialization.NBT_COMPOUND_ID)) {
            NBTTagCompound containedItemNbt = nbtCompound.getCompoundTag(CONTAINED_ITEM);
            ItemStack containedItem = ItemStack.loadItemStackFromNBT(containedItemNbt);
            if (!InventoryUtil.isEmptyItemStack(containedItem)) {
                instance.setContainedItem(containedItem);
            }
        }

        if (nbtCompound.hasKey(PROPERTY_OVERRIDES, Serialization.NBT_COMPOUND_ID)) {
            Map<ResourceLocation, Float> propertyOverrides = new HashMap<>();
            NBTTagCompound propertyOverridesNbt = nbtCompound.getCompoundTag(PROPERTY_OVERRIDES);
            for (String propertyOverrideKey : propertyOverridesNbt.getKeySet()) {
                if (!propertyOverridesNbt.hasKey(propertyOverrideKey, Serialization.NBT_FLOAT_ID)) {
                    continue;
                }
                float propertyOverrideValue = propertyOverridesNbt.getFloat(propertyOverrideKey);
                propertyOverrides.put(new ResourceLocation(propertyOverrideKey), propertyOverrideValue);
            }
            instance.setPropertyOverrides(propertyOverrides);
        }
    }

}
