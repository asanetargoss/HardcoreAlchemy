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

package targoss.hardcorealchemy.incantation;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import targoss.hardcorealchemy.HardcoreAlchemyCore;
import targoss.hardcorealchemy.incantation.api.ISpell;
import targoss.hardcorealchemy.item.Items;
import targoss.hardcorealchemy.network.MessageFakeBlockBreak;
import targoss.hardcorealchemy.util.MiscVanilla;

public class SpellCreateSlate implements ISpell {
    protected Random random = new Random();

    @Override
    public boolean canInvoke(EntityPlayerMP player) {
        RayTraceResult pick = ForgeHooks.rayTraceEyes(player, MiscVanilla.getPlayerReachDistance(player) + 1);
        if (pick == null || pick.typeOfHit != RayTraceResult.Type.BLOCK) {
            return false;
        }
        BlockPos pos = pick.getBlockPos();
        World world = player.world;
        IBlockState blockState = world.getBlockState(pos);
        if (blockState == null) {
            return false;
        }
        Block block = blockState.getBlock();
        if (block == null) {
            return false;
        }
        return block == Blocks.STONE;
    }

    @Override
    public void invoke(EntityPlayerMP player) {
        ItemStack itemStack = new ItemStack(Items.EMPTY_SLATE);
        RayTraceResult pick = ForgeHooks.rayTraceEyes(player, MiscVanilla.getPlayerReachDistance(player) + 1);
        
        // Spawn the slate
        if (pick.typeOfHit != RayTraceResult.Type.BLOCK) {
            player.dropItem(itemStack, false, true);
        }
        Vec3d spawnPos = new Vec3d(pick.sideHit.getDirectionVec()).scale(0.3).add(pick.hitVec);
        float spawnSpeed = 0.03F + (0.03F * random.nextFloat());
        if (pick.sideHit == EnumFacing.UP) {
            spawnSpeed += 0.15F;
        }
        Vec3d spawnVelocity = new Vec3d(pick.sideHit.getDirectionVec()).scale(spawnSpeed);
        EntityItem entityItem = new EntityItem(player.world, spawnPos.xCoord, spawnPos.yCoord, spawnPos.zCoord, itemStack);
        entityItem.setPickupDelay(40);
        entityItem.motionX = spawnVelocity.xCoord;
        entityItem.motionY = spawnVelocity.yCoord;
        entityItem.motionZ = spawnVelocity.zCoord;
        player.world.spawnEntity(entityItem);
        
        // Create block breaking effect
        BlockPos pos = pick.getBlockPos();
        IBlockState blockState = player.world.getBlockState(pos);
        if (blockState != null) {
            Block block = blockState.getBlock();
            if (block != null) {
                int blockID = Block.getIdFromBlock(block);
                int blockStateID = Block.getStateId(blockState);
                NetworkRegistry.TargetPoint targetPoint = new NetworkRegistry.TargetPoint(player.dimension, pos.getX(), pos.getY(), pos.getZ(), 100);
                HardcoreAlchemyCore.proxy.messenger.sendToAllAround(new MessageFakeBlockBreak(blockID, blockStateID, pos.getX(), pos.getY(), pos.getZ()), targetPoint);
            }
        }
    }
}
