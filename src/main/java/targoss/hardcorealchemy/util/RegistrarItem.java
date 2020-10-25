/*
 * Copyright 2020 asanetargoss
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

package targoss.hardcorealchemy.util;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import targoss.hardcorealchemy.coremod.PreInitLogger;

public class RegistrarItem extends RegistrarForge<Item> {

    public RegistrarItem(String name, String namespace, PreInitLogger logger) {
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
