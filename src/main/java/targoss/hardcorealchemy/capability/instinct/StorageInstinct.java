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

package targoss.hardcorealchemy.capability.instinct;

import static targoss.hardcorealchemy.util.Serialization.NBT_COMPOUND_ID;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.ModStateException;
import targoss.hardcorealchemy.capability.instinct.ICapabilityInstinct.InstinctEntry;
import targoss.hardcorealchemy.instinct.Instincts;
import targoss.hardcorealchemy.instinct.api.IInstinctEffectData;
import targoss.hardcorealchemy.instinct.api.IInstinctState;
import targoss.hardcorealchemy.instinct.api.InstinctEffect;
import targoss.hardcorealchemy.instinct.internal.InstinctEffectWrapper;
import targoss.hardcorealchemy.instinct.internal.InstinctNeedWrapper;
import targoss.hardcorealchemy.instinct.internal.InstinctState;
import targoss.hardcorealchemy.util.Serialization;

public class StorageInstinct implements Capability.IStorage<ICapabilityInstinct> {
    public static final String INSTINCT = "instinct";
    public static final String ENABLED = "enabled";
    public static final String INACTIVE_INSTINCT_TIME = "inactiveInstinctTime";

    public static final String INSTINCTS = "instincts";
    public static final String INSTINCT_ID = "id";
    
    public static final String NEEDS = "needs";
    public static final String NEED_ID = "id";
    /** Key for user-defined need data */
    public static final String NEED_DATA = "data";
    
    public static final String INSTINCT_STATE = "instinct_state";
    public static final String INSTINCT_STATE_NEED_STATUS = "need_status";
    public static final String INSTINCT_STATE_EFFECT_AMPLIFIERS = "effect_amplifiers";
    public static final String EFFECT_AMPLIFIER_ID = "id";
    public static final String EFFECT_AMPLIFIER_AMOUNT = "amount";
    
    public static final String EFFECTS = "effects";
    public static final String EFFECT_ID = "id";
    public static final String EFFECT_AMPLIFIER = "amplifier";
    public static final String EFFECT_MAX_INSTINCT = "max_instinct";
    
    public static final String ACTIVE_EFFECTS = "active_effects";
    
    public static final String EFFECT_DATA = "effect_data";
    public static final String EFFECT_DATA_EFFECT_ID = "id";
    public static final String EFFECT_DATA_DATA = "data";
    
    @Override
    public NBTBase writeNBT(Capability<ICapabilityInstinct> capability, ICapabilityInstinct instance, EnumFacing side) {
        NBTTagCompound nbtCompound = new NBTTagCompound();
        
        nbtCompound.setFloat(INSTINCT, instance.getInstinct());
        nbtCompound.setBoolean(ENABLED, instance.getEnabled());
        nbtCompound.setFloat(INACTIVE_INSTINCT_TIME, instance.getInstinctMessageTime());
        
        NBTTagList instinctsNBT = new NBTTagList();
        for (InstinctEntry entry : instance.getInstincts()) {
            instinctsNBT.appendTag(serializeInstinctEntry(entry));
        }
        nbtCompound.setTag(INSTINCTS, instinctsNBT);
        
        NBTTagList activeEffectsNBT = new NBTTagList();
        for (InstinctEffectWrapper wrapper : instance.getActiveEffects().values()) {
            if (wrapper.effect == null) {
                HardcoreAlchemy.LOGGER.warn("An effect wrapper is missing an effect. It will not be serialized to NBT.");
                continue;
            }
            activeEffectsNBT.appendTag(serializeEffectWrapper(wrapper));
        }
        nbtCompound.setTag(ACTIVE_EFFECTS, activeEffectsNBT);
        
        NBTTagList effectDataNBT = serializeEffectData(instance.getEffectData());
        nbtCompound.setTag(EFFECT_DATA, effectDataNBT);
        
        return nbtCompound;
    }

    @Override
    public void readNBT(Capability<ICapabilityInstinct> capability, ICapabilityInstinct instance, EnumFacing side,
            NBTBase nbt) {
        NBTTagCompound nbtCompound = (nbt instanceof NBTTagCompound) ? (NBTTagCompound)nbt : new NBTTagCompound();
        
        instance.setInstinct(nbtCompound.getFloat(INSTINCT));
        if (nbtCompound.hasKey(ENABLED)) {
            // Enabled defaults to true, so be careful not to override the value
            instance.setEnabled(nbtCompound.getBoolean(ENABLED));
        }
        instance.setInstinctMessageTime(nbtCompound.getInteger(INACTIVE_INSTINCT_TIME));
        
        List<InstinctEntry> instincts = instance.getInstincts();
        instincts.clear();
        NBTTagList instinctsNBT = nbtCompound.getTagList(INSTINCTS, NBT_COMPOUND_ID);
        int numInstincts = instinctsNBT.tagCount();
        for (int i = 0; i < numInstincts; i++) {
            InstinctEntry entry = deserializeInstinctEntry(instinctsNBT.getCompoundTagAt(i));
            if (entry.instinct == null) {
                HardcoreAlchemy.LOGGER.warn("An instinct is undefined. It will be ignored.");
            }
            else {
                instincts.add(entry);
            }
        }
        
        Map<InstinctEffect, InstinctEffectWrapper> activeEffects = instance.getActiveEffects();
        activeEffects.clear();
        NBTTagList activeEffectsNBT = nbtCompound.getTagList(ACTIVE_EFFECTS, NBT_COMPOUND_ID);
        int numActiveEffects = activeEffectsNBT.tagCount();
        for (int i = 0; i < numActiveEffects; i++) {
            InstinctEffectWrapper effectWrapper = deserializeEffectWrapper(activeEffectsNBT.getCompoundTagAt(i));
            if (effectWrapper.effect == null) {
                HardcoreAlchemy.LOGGER.warn("An active instinct effect is undefined. It will be ignored.");
                continue;
            }
            activeEffects.put(effectWrapper.effect, effectWrapper);
        }
        
        NBTTagList uninitializeEffectDataNBT = nbtCompound.getTagList(EFFECT_DATA, NBT_COMPOUND_ID);
        Map<InstinctEffect, NBTTagCompound> uninitializedEffectData = deserializeUninitializedEffectData(uninitializeEffectDataNBT);
        instance.setUninitializedEffectData(uninitializedEffectData);
    }
    
    public static NBTTagCompound serializeInstinctEntry(InstinctEntry entry) {
        NBTTagCompound instinctNBT = new NBTTagCompound();
        
        instinctNBT.setString(INSTINCT_ID, entry.instinct.getRegistryName().toString());
        
        if (entry.needs != null) {
            // Can be null if not initialized, so we need to check for that
            NBTTagList needsNBT = new NBTTagList();
            for (InstinctNeedWrapper wrapper : entry.needs) {
                if (wrapper.factory == null) {
                    HardcoreAlchemy.LOGGER.warn("An instinct need is missing a factory. It will not be saved to NBT.");
                    continue;
                }
                needsNBT.appendTag(serializeNeedWrapper(wrapper));
            }
            instinctNBT.setTag(NEEDS, needsNBT);
        }
        
        if (entry.effects != null) {
            // Can be null if not initialized, so we need to check for that
            NBTTagList effectsNBT = new NBTTagList();
            for (InstinctEffectWrapper wrapper : entry.effects) {
                effectsNBT.appendTag(serializeEffectWrapper(wrapper));
            }
            instinctNBT.setTag(EFFECTS, effectsNBT);
        }
        
        return instinctNBT;
    }
    
    public static InstinctEntry deserializeInstinctEntry(NBTTagCompound nbt) {
        InstinctEntry instinctEntry = new InstinctEntry();
        
        instinctEntry.instinct = Instincts.REGISTRY.getValue(new ResourceLocation(nbt.getString(INSTINCT_ID)));
        if (instinctEntry.instinct == null) {
            HardcoreAlchemy.LOGGER.warn("Could not find instinct ' " + nbt.getString(INSTINCT_ID) + "' from instinct NBT");
            return instinctEntry;
        }
        
        if (nbt.hasKey(NEEDS)) {
            // Only set if the key exists. Otherwise, needs is null so the system should initialize it at runtime
            List<InstinctNeedWrapper> needs = new ArrayList<>();
            NBTTagList needsNBT = nbt.getTagList(NEEDS, NBT_COMPOUND_ID);
            int numNeeds = needsNBT.tagCount();
            for (int i = 0; i < numNeeds; i++) {
                InstinctNeedWrapper wrapper = deserializeNeedWrapper(needsNBT.getCompoundTagAt(i));
                if (wrapper.factory == null) {
                    // Do not add the wrapper if the factory is not defined, i.e. is not in the registry
                    HardcoreAlchemy.LOGGER.warn("An instinct need read from NBT is missing the factory. It will be ignored.");
                    continue;
                }
                needs.add(wrapper);
            }
            instinctEntry.setNeeds(needs);
        }
        
        if (nbt.hasKey(EFFECTS)) {
            // Only set if the key exists. Otherwise, effects is null so the system should initialize it at runtime
            List<InstinctEffectWrapper> effects = new ArrayList<>();
            NBTTagList effectsNBT = nbt.getTagList(EFFECTS, NBT_COMPOUND_ID);
            int numEffects = effectsNBT.tagCount();
            for (int i = 0; i < numEffects; i++) {
                InstinctEffectWrapper wrapper = deserializeEffectWrapper(effectsNBT.getCompoundTagAt(i));
                if (wrapper.effect == null) {
                    HardcoreAlchemy.LOGGER.warn("An inactive/candidate instinct effect is undefined. It will be ignored.");
                    continue;
                }
                // Only add the wrapper if the effect is defined, i.e. is in the registry
                effects.add(wrapper);
            }
            instinctEntry.setEffects(effects);
        }
        
        return instinctEntry;
    }
    
    public static NBTTagCompound serializeNeedWrapper(InstinctNeedWrapper wrapper) {
        NBTTagCompound needNBT = new NBTTagCompound();
        
        needNBT.setString(NEED_ID, wrapper.factory.getRegistryName().toString());
        if (wrapper.need != null) {
            needNBT.setTag(NEED_DATA, wrapper.need.serializeNBT());
        }
        else if (wrapper.needData != null) {
            // Avoid the small edge case where the need was never initialized and so the data is still only stored as nbt
            needNBT.setTag(NEED_DATA, wrapper.needData);
        }
        
        if (wrapper.state != null) {
            needNBT.setTag(INSTINCT_STATE, serializeInstinctState(wrapper.state));
        }
        
        return needNBT;
    }
    
    public static InstinctNeedWrapper deserializeNeedWrapper(NBTTagCompound nbt) {
        InstinctNeedWrapper wrapper = new InstinctNeedWrapper();
        
        wrapper.factory = Instincts.NEED_FACTORY_REGISTRY.getValue(new ResourceLocation(nbt.getString(NEED_ID)));
        if (wrapper.factory == null) {
            HardcoreAlchemy.LOGGER.warn("Could not find factory '" + nbt.getString(NEED_ID) + "' from instinct need NBT");
            return wrapper;
        }
        
        // We can't deserialize properly here because we don't have enough information yet to initialize the need
        if (nbt.hasKey(NEED_DATA)) {
            wrapper.needData = nbt.getCompoundTag(NEED_DATA);
        }
        
        if (nbt.hasKey(INSTINCT_STATE)) {
            wrapper.state = deserializeInstinctState(nbt.getCompoundTag(INSTINCT_STATE));
        }
        
        
        return wrapper;
    }
    
    public static NBTTagCompound serializeInstinctState(InstinctState state) {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setByte(INSTINCT_STATE_NEED_STATUS, (byte)state.needStatus.ordinal());
        
        NBTTagList amplifiersNBT = new NBTTagList();
        for (Map.Entry<InstinctEffect, Float> entry : state.effectAmplifiers.entrySet()) {
            NBTTagCompound amplifierNBT = new NBTTagCompound();
            amplifierNBT.setString(EFFECT_AMPLIFIER_ID, entry.getKey().getRegistryName().toString());
            amplifierNBT.setFloat(EFFECT_AMPLIFIER_AMOUNT, entry.getValue());
            amplifiersNBT.appendTag(amplifierNBT);
        }
        
        return nbt;
    }
    
    public static InstinctState deserializeInstinctState(NBTTagCompound nbt) {
        InstinctState state = new InstinctState();
        
        byte needStatusValue = nbt.getByte(INSTINCT_STATE_NEED_STATUS);
        if (needStatusValue < 0 || needStatusValue >= IInstinctState.NeedStatus.values().length) {
            HardcoreAlchemy.LOGGER.warn("Invalid need status enum " + needStatusValue + " when deserializing instinct need data. The default " + state.needStatus.ordinal() + " will be used. (" + state.needStatus.toString() + ")");
        }
        else {
            state.needStatus = IInstinctState.NeedStatus.values()[needStatusValue];
            state.lastNeedStatus = state.needStatus;
        }
        
        NBTTagList amplifiersNBT = nbt.getTagList(INSTINCT_STATE_EFFECT_AMPLIFIERS, NBT_COMPOUND_ID);
        int numAmplifiers = amplifiersNBT.tagCount();
        for (int i = 0; i < numAmplifiers; i++) {
            NBTTagCompound amplifierNBT = amplifiersNBT.getCompoundTagAt(i);
            InstinctEffect effect = Instincts.EFFECT_REGISTRY.getValue(new ResourceLocation(amplifierNBT.getString(EFFECT_AMPLIFIER_ID)));
            if (effect == null) {
                HardcoreAlchemy.LOGGER.warn("Invalid effect name '" + amplifierNBT.getString(EFFECT_AMPLIFIER_ID) + "' when deserializing instinct effect amplifier. The effect amplifier will be ignored.");
                continue;
            }
            state.effectAmplifiers.put(effect, amplifierNBT.getFloat(EFFECT_AMPLIFIER_AMOUNT));
        }
        
        return state;
    }
    
    public static NBTTagCompound serializeEffectWrapper(InstinctEffectWrapper wrapper) {
        NBTTagCompound nbt = new NBTTagCompound();
        
        nbt.setString(EFFECT_ID, wrapper.effect.getRegistryName().toString());
        nbt.setFloat(EFFECT_AMPLIFIER, wrapper.amplifier);
        nbt.setFloat(EFFECT_MAX_INSTINCT, wrapper.maxInstinct);
        
        return nbt;
    }
    
    public static InstinctEffectWrapper deserializeEffectWrapper(NBTTagCompound nbt) {
        InstinctEffectWrapper wrapper = new InstinctEffectWrapper();
        
        wrapper.effect = Instincts.EFFECT_REGISTRY.getValue(new ResourceLocation(nbt.getString(EFFECT_ID)));
        if (wrapper.effect == null) {
            HardcoreAlchemy.LOGGER.warn("Invalid effect name '' when deserializing instinct effect wrapper.");
            return wrapper;
        }
        
        wrapper.amplifier = nbt.getFloat(EFFECT_AMPLIFIER);
        wrapper.maxInstinct = nbt.getFloat(EFFECT_MAX_INSTINCT);
        
        return wrapper;
    }
    
    public static NBTTagList serializeEffectData(Map<InstinctEffect, IInstinctEffectData> effectData) {
        NBTTagList effectDataNBT = new NBTTagList();
        
        for (Map.Entry<InstinctEffect, IInstinctEffectData> entry : effectData.entrySet()) {
            NBTTagCompound dataNBT = entry.getValue().serializeNBT();
            if (dataNBT != null) {
                NBTTagCompound dataContainerNBT = new NBTTagCompound();
                dataContainerNBT.setString(EFFECT_DATA_EFFECT_ID, entry.getKey().getRegistryName().toString());
                dataContainerNBT.setTag(EFFECT_DATA_DATA, dataNBT);
                effectDataNBT.appendTag(dataContainerNBT);
            }
        }
        
        return effectDataNBT;
    }
    
    public static Map<InstinctEffect, NBTTagCompound> deserializeUninitializedEffectData(NBTTagList nbt) {
        Map<InstinctEffect, NBTTagCompound> uninitializedEffectData = new HashMap<>();
        
        int n = nbt.tagCount();
        for (int i = 0; i < n; i++) {
            NBTTagCompound dataContainerNBT = nbt.getCompoundTagAt(i);
            InstinctEffect effect = Instincts.EFFECT_REGISTRY.getValue(new ResourceLocation(dataContainerNBT.getString(EFFECT_DATA_EFFECT_ID)));
            if (effect != null) {
                NBTTagCompound dataNBT = dataContainerNBT.getCompoundTag(EFFECT_DATA_DATA);
                if (dataNBT != null) {
                    uninitializedEffectData.put(effect, dataNBT);
                }
            }
        }
        
        return uninitializedEffectData;
    }
}
