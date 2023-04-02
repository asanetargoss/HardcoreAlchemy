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

package targoss.hardcorealchemy.research;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.IForgeRegistry;
import net.minecraftforge.fml.common.registry.RegistryBuilder;
import targoss.hardcorealchemy.HardcoreAlchemyCore;
import targoss.hardcorealchemy.coremod.HardcoreAlchemyPreInit;
import targoss.hardcorealchemy.registrar.Registrar;
import targoss.hardcorealchemy.registrar.RegistrarKnowledgeFact;

public class Studies {
    public static final Registrar<KnowledgeFact> KNOWLEDGE_FACTS = new RegistrarKnowledgeFact("knowledge_facts", HardcoreAlchemyCore.MOD_ID, HardcoreAlchemyPreInit.LOGGER);
    
    public static final IForgeRegistry<KnowledgeFact> KNOWLEDGE_FACT_REGISTRY = new RegistryBuilder<KnowledgeFact>()
            .setName(new ResourceLocation(HardcoreAlchemyCore.MOD_ID, "knowledge_facts"))
            .setType(KnowledgeFact.class)
            .setIDRange(0, 2048)
            .create();
}
