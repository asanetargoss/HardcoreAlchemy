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

package targoss.hardcorealchemy.research;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.IForgeRegistry;
import net.minecraftforge.fml.common.registry.RegistryBuilder;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.coremod.HardcoreAlchemyPreInit;
import targoss.hardcorealchemy.registrar.Registrar;
import targoss.hardcorealchemy.registrar.RegistrarKnowledgeFact;

public class Studies {
    public static final Registrar<KnowledgeFact> KNOWLEDGE_FACTS = new RegistrarKnowledgeFact("knowledge_facts", HardcoreAlchemy.MOD_ID, HardcoreAlchemyPreInit.LOGGER);
    
    public static final IForgeRegistry<KnowledgeFact> KNOWLEDGE_FACT_REGISTRY = new RegistryBuilder<KnowledgeFact>()
            .setName(new ResourceLocation(HardcoreAlchemy.MOD_ID, "knowledge_facts"))
            .setType(KnowledgeFact.class)
            .setIDRange(0, 2048)
            .create();

    public static final KnowledgeFact FACT_DIRTY_WATER_WARNING = KNOWLEDGE_FACTS.add("dirty_water_warning", new KnowledgeFact(false));
    public static final KnowledgeFact FACT_MAGIC_INHIBITION_WARNING = KNOWLEDGE_FACTS.add("magic_inhibition_warning", new KnowledgeFact(false));
}
