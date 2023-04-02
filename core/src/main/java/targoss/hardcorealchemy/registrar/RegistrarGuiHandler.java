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

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import targoss.hardcorealchemy.gui.IndexedGuiHandler;

public class RegistrarGuiHandler extends Registrar<IndexedGuiHandler> {
    public RegistrarGuiHandler(String name, String namespace, Logger logger) {
        super(name, namespace, logger);
    }
    
    public <V extends IndexedGuiHandler> V add(String entryName, V entry) {
        entry = super.add(entryName, entry);
        entry.index = entries.size() - 1;
        entry.id = new ResourceLocation(namespace, entryName);
        return entry;
    }
    
    public class GuiHandler implements IGuiHandler {
        @Override
        public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
            if (ID > entries.size() - 1) {
                return null;
            }
            return entries.get(ID).handler.getServerGuiElement(ID, player, world, x, y, z);
        }

        @Override
        public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
            return entries.get(ID).handler.getClientGuiElement(ID, player, world, x, y, z);
        }
    }
    
    public final GuiHandler guiHandler = new GuiHandler();
    
    @Override
    public boolean register(int phase) {
        if (!super.register(phase)) {
            return false;
        }
        if (phase != 0) {
            return false;
        }
        NetworkRegistry.INSTANCE.registerGuiHandler(namespace, guiHandler);
        return true;
    }
}
