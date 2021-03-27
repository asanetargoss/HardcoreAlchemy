/*
 * Copyright 2021 asanetargoss
 * 
 * This file is part of the Hardcore Alchemy capstone mod.
 * 
 * The Hardcore Alchemy capstone mod is free software: you can redistribute
 * it and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 3 of the
 * License.
 * 
 * The Hardcore Alchemy capstone mod is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the Hardcore Alchemy capstone mod. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.capability.research;

import static targoss.hardcorealchemy.util.Serialization.NBT_LIST_ID;
import static targoss.hardcorealchemy.util.Serialization.NBT_STRING_ID;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import targoss.hardcorealchemy.research.KnowledgeFact;
import targoss.hardcorealchemy.research.Studies;

public class StorageResearch implements Capability.IStorage<ICapabilityResearch>  {
    protected static final String KNOWLEDGE_FACTS = "knowledge_facts";

    @Override
    public NBTBase writeNBT(Capability<ICapabilityResearch> capability, ICapabilityResearch instance, EnumFacing side) {
        NBTTagCompound nbt = new NBTTagCompound();
        
        NBTTagList factsNBT = new NBTTagList();
        for (KnowledgeFact fact : instance.getKnowledgeFacts()) {
            NBTTagString factNBT = new NBTTagString(fact.getRegistryName().toString());
            factsNBT.appendTag(factNBT);
        }
        nbt.setTag(KNOWLEDGE_FACTS, factsNBT);
        
        return nbt;
    }

    @Override
    public void readNBT(Capability<ICapabilityResearch> capability, ICapabilityResearch instance, EnumFacing side,
            NBTBase nbt) {
        if (!(nbt instanceof NBTTagCompound)) {
            return;
        }
        NBTTagCompound nbtCompound = (NBTTagCompound)nbt;

        if (nbtCompound.hasKey(KNOWLEDGE_FACTS, NBT_LIST_ID)) {
            NBTTagList factsNBT = nbtCompound.getTagList(KNOWLEDGE_FACTS, NBT_STRING_ID);
            int n = factsNBT.tagCount();
            Set<KnowledgeFact> facts = new HashSet<>(n);
            for (int i = 0; i < n; ++i) {
                String factString = factsNBT.getStringTagAt(i);
                ResourceLocation factRes = new ResourceLocation(factString);
                KnowledgeFact fact = Studies.KNOWLEDGE_FACT_REGISTRY.getValue(factRes);
                if (fact != null) {
                    facts.add(fact);
                }
            }
            instance.setKnowledgeFacts(facts);
        }
    }

}
