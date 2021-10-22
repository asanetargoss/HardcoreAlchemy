/*
 * Copyright 2017-2018 asanetargoss
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

package targoss.hardcorealchemy.magic.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.world.DimensionType;

public class MobLevelRange {
    private static final Map<Integer, List<MobLevelRange>> rangesByDimension = new HashMap();
    public static MobLevelRange defaultLevelRange = new MobLevelRange(1, 50);
    public static List<MobLevelRange> defaultLevelRanges = new ArrayList();
    
    static {
        // The End and other dimensions are assumed to be hard to get to, so allow high levels
        defaultLevelRanges.add(defaultLevelRange);
        
        // Overworld mobs should increase in level as one goes deeper underground
        {
            int id = DimensionType.OVERWORLD.getId();
            new MobLevelRange(id, 0.0D, 30.0D, 25, 35);
            new MobLevelRange(id, 30.0D, 50.0D, 20, 30);
            new MobLevelRange(id, 50.0D, 256.0D, 1, 20);
        }
        // Nether mobs should increase in level as one rises to Nether Fortress levels
        {
            int id = DimensionType.NETHER.getId();
            new MobLevelRange(id, 0.0D, 40.0D, 10, 30);
            new MobLevelRange(id, 40.0D, 70.0D, 20, 40);
            new MobLevelRange(id, 70.0D, 256.0D, 30, 50);
        }
    }
    
    public final double minY;
    public final double maxY;
    public final int minLevel;
    public final int maxLevel;
    /**
     * May be null
     */
    private final Integer dimensionID;
    
    public MobLevelRange(int minLevel, int maxLevel) {
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.minY = -Integer.MAX_VALUE;
        this.maxY = Integer.MAX_VALUE;
        this.dimensionID = null;
    }
    
    public MobLevelRange(int dimensionID, double minY, double maxY, int minLevel, int maxLevel) {
        this.dimensionID = dimensionID;
        this.minY = minY;
        this.maxY = maxY;
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        
        List<MobLevelRange> dimensionRange;
        if (rangesByDimension.containsKey(dimensionID)) {
            dimensionRange = rangesByDimension.get(dimensionID);
        }
        else {
            dimensionRange = new ArrayList<MobLevelRange>();
            rangesByDimension.put(dimensionID, dimensionRange);
        }
        dimensionRange.add(this);
    }
    
    public static List<MobLevelRange> getRangesByDimension(int dimensionID) {
        List<MobLevelRange> dimensionRange = rangesByDimension.get(dimensionID);
        if (dimensionRange != null) {
            return dimensionRange;
        }
        return defaultLevelRanges;
    }
    
    public static MobLevelRange getRange(int dimensionID, double posY) {
        List<MobLevelRange> levelRanges = getRangesByDimension(dimensionID);
        for (MobLevelRange levelRange : levelRanges) {
            if (posY >= levelRange.minY && posY <= levelRange.maxY) {
                return levelRange;
            }
        }
        return defaultLevelRange;
    }
    
    private Random random = new Random(443820878);
    
    public int getRandomLevel(double posX, double posZ, long worldSeed) {
        // Two octave procedural level selection based on 4 chunk and 16 chunk square regions
        // The procedural part is 75% of the level spread
        int posXint = (int)posX;
        int posZint = (int)posZ;
        int regionX = posXint / 64;
        int regionZ = posZint / 64;
        int regionXBig = posXint / 256;
        int regionZBig = posZint / 256;
        
        long resultInt = worldSeed*4841746148138200878L + 1866608848353548427L;
        long resultIntBig = resultInt;
        resultInt += regionX;
        resultInt = resultInt*4841746148138200878L + 1866608848353548427L;
        resultInt += regionZ;
        resultInt = resultInt*4841746148138200878L + 1866608848353548427L;
        resultIntBig += regionXBig;
        resultIntBig = resultIntBig*4841746148138200878L + 1866608848353548427L;
        resultIntBig += regionZBig;
        resultIntBig = resultIntBig*4841746148138200878L + 1866608848353548427L;
        
        float result = Math.abs((float)resultInt / (float)Long.MAX_VALUE);
        float resultBig = Math.abs((float)resultIntBig / (float)Long.MAX_VALUE);
        
        // Should be a number between 0 and 1 if all goes well
        float regionFraction = (result + resultBig)*0.5F;
        
        // Add the 25% random component, which adds variation for each individual mob that spawns
        float randomFraction = random.nextFloat();
        float levelFraction = randomFraction*0.25F + regionFraction*0.75F;
        
        // Using the number created between 0 and 1, determine a final level
        int level = (int)Math.round(
            ((float)this.minLevel)*(1.0F-levelFraction) +
            ((float)this.maxLevel)*levelFraction
        );
        return level;
    }
    
}