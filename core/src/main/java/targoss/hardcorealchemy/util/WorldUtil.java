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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class WorldUtil {
    public static enum CollisionMethod {
        FULL,
        FIRE,
        LAVA
    }
    
    public static class CollisionPredicate {
        public static final Predicate<BlockInfo> FIRE = new Predicate<BlockInfo>() {
            @Override
            public boolean test(BlockInfo info) {
                return info.getState().getBlock() == net.minecraft.init.Blocks.FIRE;
            }
        };
        public static final Predicate<BlockInfo> LAVA = new Predicate<BlockInfo>() {
            @SuppressWarnings("deprecation")
            @Override
            public boolean test(BlockInfo info) {
                return info.getState().getBlock().getMaterial(info.getState()) == Material.LAVA;
            }
        };
    }
    
    public static class BlockInfo {
        protected BlockPos pos;
        protected IBlockState state;
        public BlockInfo(BlockPos pos, IBlockState state) {
            this.pos = pos == null ? null : pos.toImmutable();
            this.state = state;
        }
        public BlockInfo(MutableBlockInfo other) {
            this.pos = other.pos == null ? null : other.pos.toImmutable();
            this.state = other.state;
        }
        public BlockPos getPos() {
            return pos;
        }
        public IBlockState getState() {
            return state;
        }
        public BlockInfo toImmutable() {
            return this;
        }
    }
    
    public static class MutableBlockInfo extends BlockInfo {
        public MutableBlockInfo() {
            super(null, null);
        }
        public MutableBlockInfo(BlockPos pos, IBlockState state) {
            super(pos, state);
        }
        public void setPos(BlockPos pos) {
            this.pos = pos;
        }
        public void setState(IBlockState state) {
            this.state = state;
        }
        @Override
        public BlockInfo toImmutable() {
            return new BlockInfo(this);
        }
    }
    
    /**
     * Ignores air/anything null.
     * Need CollisionMethod because Minecraft is inconsistent about colliding with things.
     * */
    public static List<BlockInfo> getCollidingStates(Entity entity, Predicate<BlockInfo> predicate, CollisionMethod collisionMethod) {
        List<BlockInfo> matching = new ArrayList<>(); 
        
        AxisAlignedBB box;
        switch (collisionMethod) {
        case FIRE:
            box = entity.getEntityBoundingBox().contract(0.001);
            break;
        case LAVA:
            box = entity.getEntityBoundingBox().expand(-0.1, -0.4, -0.1);
            break;
        default:
            box = entity.getEntityBoundingBox();
            break;
        }
        
        int xMin = MathHelper.floor(box.minX);
        int xMax = MathHelper.ceil(box.maxX);
        int yMin = MathHelper.floor(box.minY);
        int yMax = MathHelper.ceil(box.maxY);
        int zMin = MathHelper.floor(box.minZ);
        int zMax = MathHelper.ceil(box.maxZ);
        BlockPos.PooledMutableBlockPos mPos = BlockPos.PooledMutableBlockPos.retain();
        MutableBlockInfo blockInfo = new MutableBlockInfo();
        for (int x = xMin; x < xMax; ++x) {
            for (int y = yMin; y < yMax; ++y) {
                for (int z = zMin; z < zMax; ++z) {
                    IBlockState state = entity.world.getBlockState(mPos.setPos(x, y, z));
                    if (state == null) {
                        continue;
                    }
                    Block block = state.getBlock();
                    if (block == null || block.isAir(state, entity.world, mPos)) {
                        continue;
                    }
                    blockInfo.setPos(mPos);
                    blockInfo.setState(state);
                    if (predicate.test(blockInfo)) {
                        matching.add(blockInfo.toImmutable());
                    }
                }
            }
        }
        mPos.release();
        
        return matching;
    }
    
    /**
     * Ignores air/anything null.
     * Need CollisionMethod because Minecraft is inconsistent about colliding with things.
     * */
    public static boolean doesCollide(Entity entity, Predicate<BlockInfo> predicate, CollisionMethod collisionMethod) {
        AxisAlignedBB box;
        switch (collisionMethod) {
        case FIRE:
            box = entity.getEntityBoundingBox().contract(0.001);
            break;
        case LAVA:
            box = entity.getEntityBoundingBox().expand(-0.1, -0.4, -0.1);
            break;
        default:
            box = entity.getEntityBoundingBox();
            break;
        }
        
        int xMin = MathHelper.floor(box.minX);
        int xMax = MathHelper.ceil(box.maxX);
        int yMin = MathHelper.floor(box.minY);
        int yMax = MathHelper.ceil(box.maxY);
        int zMin = MathHelper.floor(box.minZ);
        int zMax = MathHelper.ceil(box.maxZ);
        BlockPos.PooledMutableBlockPos mPos = BlockPos.PooledMutableBlockPos.retain();
        MutableBlockInfo blockInfo = new MutableBlockInfo();
        for (int x = xMin; x < xMax; ++x) {
            for (int y = yMin; y < yMax; ++y) {
                for (int z = zMin; z < zMax; ++z) {
                    IBlockState state = entity.world.getBlockState(mPos.setPos(x, y, z));
                    if (state == null) {
                        continue;
                    }
                    Block block = state.getBlock();
                    if (block == null || block.isAir(state, entity.world, mPos)) {
                        continue;
                    }
                    blockInfo.setPos(mPos);
                    blockInfo.setState(state);
                    if (predicate.test(blockInfo)) {
                        return true;
                    }
                }
            }
        }
        mPos.release();
        
        return false;
    }
    
    public static @Nullable BlockInfo getRandomCollidingState(Random random, Entity entity, Predicate<BlockInfo> predicate, CollisionMethod collisionMethod) {
        List<BlockInfo> matching = getCollidingStates(entity, predicate, collisionMethod);
        if (matching.isEmpty()) {
            return null;
        }
        return matching.get(random.nextInt(matching.size()));
    }
    
    public static @Nullable BlockPos getFireFuelPos(World world, BlockPos firePos) {
        BlockPos burnPos;
        burnPos = firePos.down();
        if (Blocks.FIRE.canCatchFire(world, burnPos, EnumFacing.UP)) {
            return burnPos;
        }
        burnPos = firePos.west();
        if (Blocks.FIRE.canCatchFire(world, burnPos, EnumFacing.EAST)) {
            return burnPos;
        }
        burnPos = firePos.east();
        if (Blocks.FIRE.canCatchFire(world, burnPos, EnumFacing.WEST)) {
            return burnPos;
        }
        burnPos = firePos.north();
        if (Blocks.FIRE.canCatchFire(world, burnPos, EnumFacing.SOUTH)) {
            return burnPos;
        }
        burnPos = firePos.south();
        if (Blocks.FIRE.canCatchFire(world, burnPos, EnumFacing.NORTH)) {
            return burnPos;
        }
        burnPos = firePos.up();
        if (Blocks.FIRE.canCatchFire(world, burnPos, EnumFacing.DOWN)) {
            return burnPos;
        }
        return null;
    }
}
