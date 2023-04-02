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

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.obj.OBJLoader;
import targoss.hardcorealchemy.block.BlockModelInfo;

public class RegistrarBlockModel extends Registrar<BlockModelInfo<?>> {
    public static final int DOMAIN = 0;
    public static final int RESOURCE = 1;
    
    public RegistrarBlockModel(String name, String namespace, Logger logger) {
        super(name, namespace, logger);
    }

    @Override
    public <V extends BlockModelInfo<?>> V add(String entryName, V entry) {
        super.add(entryName, entry);
        entry.id = new ResourceLocation(namespace, "block/" + entryName + ".obj");
        return entry;
    }
    
    @Override
    public boolean register(int phase) {
        if (!super.register(phase)) {
            return false;
        }
        
        switch (phase) {
        case DOMAIN:
            OBJLoader.INSTANCE.addDomain(namespace);
            break;
        case RESOURCE:
            for (BlockModelInfo<?> entry : entries) {
                ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(entry.block), 0, new ModelResourceLocation(entry.block.getRegistryName(), "inventory"));
            }
            break;
        default:
            return false;
        }
        
        return true;
    }
    
    public static class Client extends Registrar<BlockModelInfo.Client<?>> {
        public static final int TESR = 0;
        
        public Client(String name, String namespace, Logger logger) {
            super(name, namespace, logger);
        }
        
        @Override
        public boolean register(int phase) {
            if (!super.register(phase)) {
                return false;
            }
            
            switch (phase) {
            case TESR:
                for (BlockModelInfo.Client<?> entry : entries) {
                    if (entry.info.teClass != null && entry.tesr != null) {
                        entry.bindTileEntitySpecialRenderer();
                    }
                }
                break;
            default:
                return false;
            }
            
            return true;
        }
        
    }
}
