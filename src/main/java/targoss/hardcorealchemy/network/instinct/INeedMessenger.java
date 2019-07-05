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

package targoss.hardcorealchemy.network.instinct;

import io.netty.buffer.ByteBuf;
import targoss.hardcorealchemy.instinct.api.IInstinctNeed;

/**
 * Allows for custom syncing of Instinct Need data without
 * regards to how and where the Instinct Need is stored.
 * 
 * The implementation can be either stateless, or a unique instance
 * for the current need object.
 */
public interface INeedMessenger<T extends IInstinctNeed> {
    public boolean shouldSync();
    public void toBytes(T need, ByteBuf buf);
    public void fromBytes(T need, ByteBuf buf);
}
