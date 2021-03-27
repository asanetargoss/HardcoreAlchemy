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
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.registry.IForgeRegistryEntry;

public class KnowledgeFact extends IForgeRegistryEntry.Impl<KnowledgeFact> {
    protected boolean persistsOnDeath;

    public KnowledgeFact(boolean persistsOnDeath) {
        this.persistsOnDeath = persistsOnDeath;
    }
    
    public boolean getPersistsOnDeath() {
        return persistsOnDeath;
    }
    
    public ITextComponent getKnowledgeAcquireMessage() {
        ResourceLocation resource = getRegistryName();
        return new TextComponentTranslation(resource.getResourceDomain() + ".research.knowledge_fact." + resource.getResourcePath() + ".acquire");
    }
}
