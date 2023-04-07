/*
 * Copyright 2017-2023 asanetargoss
 *
 * This file is part of Hardcore Alchemy Creatures.
 *
 * Hardcore Alchemy Creatures is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Creatures is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Creatures. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.creatures.gui;

import static targoss.hardcorealchemy.util.InventoryUtil.getPlayerInventorySlots;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import targoss.hardcorealchemy.creatures.block.TileHumanityPhylactery;
import targoss.hardcorealchemy.item.ContainerItemHandler;

public class GuiHandlerHumanityPhylactery implements IGuiHandler {
    public static final int[] CONTAINER_SLOT_COORDS = {  134, 29,
                                                          80, 29,
                                                          26, 47  };
    
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileHumanityPhylactery) {
            return new ContainerItemHandler(((TileHumanityPhylactery)te).inventory, CONTAINER_SLOT_COORDS, getPlayerInventorySlots(player.inventory));
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileHumanityPhylactery) {
            TileHumanityPhylactery phyTE = (TileHumanityPhylactery)te;
            Container container = new ContainerItemHandler(phyTE.inventory, CONTAINER_SLOT_COORDS, getPlayerInventorySlots(player.inventory));
            return new GuiHumanityPhylactery(container, player.inventory.getDisplayName());
        }
        return null;
    }
}
