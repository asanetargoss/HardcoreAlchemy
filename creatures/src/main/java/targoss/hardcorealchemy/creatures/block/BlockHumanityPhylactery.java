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

package targoss.hardcorealchemy.creatures.block;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import targoss.hardcorealchemy.creatures.gui.Guis;
import targoss.hardcorealchemy.util.BlockUtil;

public class BlockHumanityPhylactery extends Block implements ITileEntityProvider {
    public BlockHumanityPhylactery() {
        super(Material.IRON, MapColor.IRON);
        setHardness(2.5F);
        setSoundType(SoundType.METAL);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        TileHumanityPhylactery te = new TileHumanityPhylactery(world);
        te.initialFrameAngle = nextInitialFrameAngle;
        nextInitialFrameAngle = 0.0F;
        return te;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
        return false;
    }

    @Override
    public boolean isBlockNormalCube(IBlockState blockState) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState blockState) {
        return false;
    }
    
    // Cache the intended placement info and use to initialize the next tile entity
    // Rotation is in radians relative to +X, rotated about +Y (+X is East)
    static float nextInitialFrameAngle = 0.0F;
    // Assume single-threaded
    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, ItemStack stack)
    {
        switch(placer.getHorizontalFacing())
        {
        case SOUTH:
            nextInitialFrameAngle = 1.5F * (float)Math.PI;
            break;
        case WEST:
            nextInitialFrameAngle = (float)Math.PI;
            break;
        case NORTH:
            nextInitialFrameAngle = 0.5F * (float)Math.PI;
            break;
        case EAST:
        default:
            nextInitialFrameAngle = 0.0F;
            break;
        
        }
        return super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, stack);
    }
    
    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            Guis.GUI_HANDLER_HUMANITY_PHYLACTERY.open(player, world, pos.getX(), pos.getY(), pos.getZ());
        }
        
        return true;
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block)
    {
        super.neighborChanged(state, world, pos, block);
        
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileHumanityPhylactery) {
            ((TileHumanityPhylactery)te).neighborChanged();
        }
    }

    /**
     * Called serverside after this block is replaced with another in Chunk, but before the Tile Entity is updated
     */
    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState blockState) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileHumanityPhylactery) {
            ((TileHumanityPhylactery)te).breakBlock(world, pos);
        }
        
        super.breakBlock(world, pos, blockState);
    }
    
    @SideOnly(Side.CLIENT)
    public boolean addHitEffects(IBlockState state, World worldObj, RayTraceResult target, net.minecraft.client.particle.ParticleManager manager)
    {
        BlockUtil.addBlockHitEffects(this, worldObj, target, manager, Blocks.MODEL_HUMANITY_PHYLACTERY_OUTER_FRAME.getBakedModel().getParticleTexture());
        return true;
    }

    @SideOnly(Side.CLIENT)
    public boolean addDestroyEffects(World world, BlockPos pos, net.minecraft.client.particle.ParticleManager manager)
    {
        BlockUtil.addBlockDestroyEffects(this, world, pos, manager, Blocks.MODEL_HUMANITY_PHYLACTERY_OUTER_FRAME.getBakedModel().getParticleTexture());
        return true;
    }
    
    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileHumanityPhylactery) {
            return ((TileHumanityPhylactery) te).isVisiblyActive() ? 15 : 0;
        }
        return super.getLightValue(state, world, pos);
    }
}
