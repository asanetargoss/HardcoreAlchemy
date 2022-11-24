/*
 * Copyright 2017-2022 asanetargoss
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

package targoss.hardcorealchemy.creatures.block;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

// TODO: See TileEntityEnderChestRenderer
// TODO: See https://wiki.mcjty.eu/modding/index.php?title=Render_Block_TESR_/_OBJ-1.9
public class BlockHeartOfForm extends Block implements ITileEntityProvider {
    public BlockHeartOfForm() {
        super(Material.IRON);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileHeartOfForm(world);
    }
    
    public static class Container extends net.minecraft.inventory.Container {
        @Override
        public boolean canInteractWith(EntityPlayer player) {
            return true;
        }
    }
    
    @Override
    public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor) {
        super.onNeighborChange(world, pos, neighbor);
        
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileHeartOfForm) {
            ((TileHeartOfForm)te).onNeighborChange(pos, neighbor);
        }
    }

    /**
     * Called serverside after this block is replaced with another in Chunk, but before the Tile Entity is updated
     */
    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState blockState) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileHeartOfForm) {
            ((TileHeartOfForm)te).breakBlock(world, pos);
        }
        
        super.breakBlock(world, pos, blockState);
    }
}