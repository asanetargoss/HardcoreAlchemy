/*
 * Copyright 2019 asanetargoss
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

package targoss.hardcorealchemy.instinct;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import targoss.hardcorealchemy.instinct.api.IInstinctNeed;
import targoss.hardcorealchemy.instinct.api.IInstinctState.NeedStatus;

public class InstinctNeedForestPlains extends InstinctNeedEnvironment {
    public InstinctNeedForestPlains(EntityLivingBase morphEntity) {
        super(morphEntity);
    }

    @Override
    public IInstinctNeed createInstanceFromMorphEntity(EntityLivingBase morphEntity) {
        if (morphEntity instanceof EntityLiving) {
            return new InstinctNeedForestPlains((EntityLiving)morphEntity);
        }
        return null;
    }
    
    @Override
    public boolean doesPlayerFeelAtHome(EntityPlayer player) {
        World world = player.world;
        MutableBlockPos blockCheckPos = new MutableBlockPos();
        boolean onOrAboveGrass = false;
        boolean sufficientLight = false;
        boolean skyAbove = false;
        boolean leavesAbove = false;
        
        // Check if standing or floating above grass (leaves are also fine)
        blockCheckPos.setPos(player);
        int grassChecksRemaining = 10;
        for (; blockCheckPos.getY() >= 0 && grassChecksRemaining > 0; blockCheckPos.move(EnumFacing.DOWN)) {
            grassChecksRemaining--;
            IBlockState stateCandidate = world.getBlockState(blockCheckPos);
            Block grassCandidate = stateCandidate.getBlock();
            Material material = grassCandidate.getMaterial(stateCandidate);
            if (grassCandidate != null && (material == Material.GRASS || material == Material.LEAVES)) {
                onOrAboveGrass = true;
                break;
            }
        }
        // No grass! Return early.
        if (!onOrAboveGrass) {
            return false;
        }
        
        // Check if sky or leaves above
        blockCheckPos.setPos(player).move(EnumFacing.UP, (int)Math.floor(player.height));
        if (world.canSeeSky(blockCheckPos)) {
            skyAbove = true;
        }
        else {
            int maxCheckHeight = Math.min(1 + world.getHeight(blockCheckPos.getX(), blockCheckPos.getZ()), 20 + blockCheckPos.getY());
            for (; blockCheckPos.getY() <= maxCheckHeight; blockCheckPos.move(EnumFacing.UP)) {
                if (world.canSeeSky(blockCheckPos)) {
                    skyAbove = true;
                    break;
                }
                IBlockState stateCandidate = world.getBlockState(blockCheckPos);
                Block grassOrSkyCandidate = stateCandidate.getBlock();
                if (grassOrSkyCandidate == null) {
                    skyAbove = true;
                    break;
                }
                Material material = grassOrSkyCandidate.getMaterial(stateCandidate);
                if (material == Material.AIR) {
                    skyAbove = true;
                    break;
                }
                if (material == Material.LEAVES) {
                    leavesAbove = true;
                    break;
                }
            }
        }
        
        // Check if sufficient light (basically, any light level
        blockCheckPos.setPos(player);
        sufficientLight = world.getLight(blockCheckPos) > 8;
        
        return onOrAboveGrass && (sufficientLight || skyAbove) && (leavesAbove || skyAbove);
    }

    @Override
    public ITextComponent getFeelsAtHomeMessage(NeedStatus needStatus) {
        return new TextComponentTranslation("hardcorealchemy.instinct.home.nature.fulfilled");
    }

    @Override
    public ITextComponent getNotAtHomeMessage(NeedStatus needStatus) {
        return new TextComponentTranslation("hardcorealchemy.instinct.home.nature.need");
    }

}
