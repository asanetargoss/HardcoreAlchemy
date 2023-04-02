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

package targoss.hardcorealchemy.registrar;

import org.apache.logging.log4j.Logger;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class RegistrarBlock extends RegistrarForge<Block> {

    public RegistrarBlock(String name, String namespace, Logger logger) {
        super(name, namespace, logger);
    }
    
    @Override
    public <V extends Block> V add(String entryName, V entry) {
        V result = super.add(entryName, entry);
        ResourceLocation loc = entry.getRegistryName();
        entry.setUnlocalizedName(loc.getResourceDomain() + "." + loc.getResourcePath());
        return result;
    }
    
    public boolean register() {
        if (!super.register()) {
            return false;
        }
        
        for (Block entry : entries) {
            GameRegistry.register(new ItemBlock(entry), entry.getRegistryName());
        }
        
        return true;
    }
}
