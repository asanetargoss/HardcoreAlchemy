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

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import targoss.hardcorealchemy.creatures.block.TileHumanityPhylactery;
import targoss.hardcorealchemy.item.ContainerItemHandler;
import targoss.hardcorealchemy.util.InventoryUtil;

public class GuiHandlerHumanityPhylactery implements IGuiHandler {
    public static final int[] CONTAINER_SLOT_COORDS = {  0,  0,
                                                        16, 16,
                                                        32, 32  };
    
    protected static List<Slot> getPlayerSlots(InventoryPlayer inv) {
        List<Slot> playerSlots = InventoryUtil.getPlayerHotbarSlots(inv);
        int n = playerSlots.size();
        for (int i = 0; i < n; ++i) {
            Slot slot = playerSlots.get(i);
            slot.xPos = i * 16;
            slot.yPos = 64;
        }
        return playerSlots;
    }
    
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileHumanityPhylactery) {
            return new ContainerItemHandler(((TileHumanityPhylactery)te).inventory, CONTAINER_SLOT_COORDS, getPlayerSlots(player.inventory));
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileHumanityPhylactery) {
            TileHumanityPhylactery phyTE = (TileHumanityPhylactery)te;
            Container container = new ContainerItemHandler(phyTE.inventory, CONTAINER_SLOT_COORDS, getPlayerSlots(player.inventory));
            return new GuiHumanityPhylactery(container, phyTE);
        }
        return null;
    }
}
