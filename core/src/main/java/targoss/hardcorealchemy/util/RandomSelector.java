/*
 * Copyright 2017-2022 asanetargoss
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

package targoss.hardcorealchemy.util;

import java.util.Arrays;
import java.util.Random;

public class RandomSelector {
    private static Random random = new Random();
    
    public static <T> T select(T[] options) {
        if (options == null) {
            throw new NullPointerException(RandomSelector.class.getName() + ": select(options, weights): options is null");
        }
        if (options.length == 0) {
            return null;
        }
        
        return options[random.nextInt(options.length)];
    }
    
    public static <T> T select(T[] options, float[] weights) {
        if (options == null) {
            throw new NullPointerException(RandomSelector.class.getName() + ": select(options, weights): options is null");
        }
        if (weights == null) {
            throw new NullPointerException(RandomSelector.class.getName() + ": select(options, weights): weights is null");
        }
        if (options.length == 0) {
            return null;
        }
        if (options.length != weights.length) {
            throw new IllegalArgumentException(RandomSelector.class.getName() + ": select(options, weights): length of options is not equal to length of weights");
        }
        /*
         * Randomly select an available option based on the given weights.
         * 
         * Visualization/example:
         * 
         * random.nextFloat()*cumulativeWeight
         * ----------------------------------------------->
         * 
         * weights[]
         * 0.5F      |1.0F               |0.7F            |
         * 
         * weightMap[]                                    |cumulativeWeight
         * 0.0F      |0.5F               |1.5F            |2.3F
         */
        int n = weights.length;
        float[] weightMap = new float[n];
        float cumulativeWeight = 0.0F;
        for (int i = 0; i < n; i++) {
            weightMap[i] = cumulativeWeight;
            cumulativeWeight += weights[i];
        }
        int selectedIndex = Arrays.binarySearch(weightMap, random.nextFloat()*cumulativeWeight);
        if (selectedIndex < 0) {
            /* Not found, but the index of the next smaller
             * value is encoded in the result.
             */
            selectedIndex = (-selectedIndex) - 2;
        }
        
        return options[selectedIndex];
    }
}
