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
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class RegistrarItem extends RegistrarForge<Item> {

    public RegistrarItem(String name, String namespace, Logger logger) {
        super(name, namespace, logger);
    }

    @Override
    public <V extends Item> V add(String itemName, V item) {
        V result = super.add(itemName, item);
        // Only used by Item.toString(), but may be useful for debugging
        result.setUnlocalizedName(item.getRegistryName().toString());
        return result;
    }
    
    @SideOnly(Side.CLIENT)
    public static void registerModel(Item item) {
        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), null));
    }
    
    @Override
    public boolean register() {
        if (!super.register()) {
            return false;
        }
        
        boolean isClient = FMLCommonHandler.instance().getSide() == Side.CLIENT;
        if (isClient) {
            for (Item item : entries) {
                registerModel(item);
            }
        }
        
        return true;
    }

}
