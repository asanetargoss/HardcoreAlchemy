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

public final class Pair<FIRST, SECOND> {
    public final FIRST first;
    public final SECOND second;
    
    public Pair(FIRST first, SECOND second) {
        this.first = first;
        this.second = second;
    }
    
    @Override
    public int hashCode() {
        // Note that this produces bad results for sequential values.
        // But... if you know your values are sequential, then why are you using a hash map?
        // If more "random" values are desired, feed the output of this through an LCG.
        return first.hashCode() ^ second.hashCode();
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        Pair<?, ?> op = (Pair<?, ?>)o;
        if (first == null) {
            if (second == null) {
                return op.first == null && op.second == null;
            }
            else {
                return op.first == null && second.equals(op.second);
            }
        }
        else {
            if (second == null) {
                return op.second == null && first.equals(op.first);
            }
            else {
                return second.equals(op.second) && first.equals(op.first);
            }
        }
    }
}
