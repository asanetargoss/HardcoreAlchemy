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

package targoss.hardcorealchemy.capability.research;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.util.ResourceLocation;
import targoss.hardcorealchemy.HardcoreAlchemyCore;
import targoss.hardcorealchemy.research.KnowledgeFact;

public class CapabilityResearch implements ICapabilityResearch {
    public static final ResourceLocation RESOURCE_LOCATION = new ResourceLocation(HardcoreAlchemyCore.MOD_ID, "research");

    protected Set<KnowledgeFact> knowledgeFacts = new HashSet<>();

    @Override
    public Set<KnowledgeFact> getKnowledgeFacts() {
        return knowledgeFacts;
    }

    @Override
    public void setKnowledgeFacts(Set<KnowledgeFact> knowledgeFacts) {
        this.knowledgeFacts = knowledgeFacts;
    }

}
