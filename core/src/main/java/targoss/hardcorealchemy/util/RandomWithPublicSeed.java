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

package targoss.hardcorealchemy.util;

import java.util.Random;

public class RandomWithPublicSeed extends Random {
    protected long publicSeed;
    
    public RandomWithPublicSeed() {
        super();
        this.publicSeed = nextLong();
        super.setSeed(publicSeed);
    }
    
    public RandomWithPublicSeed(long seed) {
        super(seed);
        this.publicSeed = seed;
    }
    
    public long getSeed() {
        return publicSeed;
    }
    
    public synchronized void setSeed(long seed) {
        super.setSeed(seed);
        publicSeed = seed;
    }
}
