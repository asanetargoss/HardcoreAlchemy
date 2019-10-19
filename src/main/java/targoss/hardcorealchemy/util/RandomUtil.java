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

package targoss.hardcorealchemy.util;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.Vec3d;

public class RandomUtil {
    public static float getRandomInRangeSigned(Random random, float min, float max) {
        float randomOut = random.nextFloat();
        if (randomOut < 0.5F) {
            return min + (randomOut * 2.0F * (max - min));
        }
        else {
            return -min - ((randomOut - 0.5F) * 2.0F * (max - min));
        }
    }
    
    @FunctionalInterface
    public static interface BlockChecker {
        boolean test(BlockPos pos);
    }
    
    public static @Nullable BlockPos findSuitableBlockPosInRangeSigned(Random random, int attempts, BlockPos center, float minDistance, float maxDistance, BlockChecker blockChecker) {
        MutableBlockPos pos = new MutableBlockPos();
        float centerX = center.getX();
        float centerY = center.getY();
        float centerZ = center.getZ();
        for (int i = 0; i < attempts; i++) {
            pos.setPos(
                centerX + getRandomInRangeSigned(random, minDistance, maxDistance),
                centerY + getRandomInRangeSigned(random, minDistance, maxDistance),
                centerZ + getRandomInRangeSigned(random, minDistance, maxDistance)
            );
            if (blockChecker.test(pos)) {
                return pos;
            }
        }
        
        return null;
    }
    
    public static Vec3d getRandomDirection(Random random) {
        float x = random.nextFloat();
        float y = random.nextFloat();
        float z = random.nextFloat();
        float s = (float)Math.sqrt((double)(x*x + y*y + z*z));
        if (s == 0.0f) {
            return new Vec3d(1.0f, 1.0f, 1.0f);
        }
        return new Vec3d(x/s, y/s, z/s);
    }
}
