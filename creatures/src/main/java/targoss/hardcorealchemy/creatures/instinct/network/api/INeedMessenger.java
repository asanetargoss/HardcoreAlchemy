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

package targoss.hardcorealchemy.creatures.instinct.network.api;

import io.netty.buffer.ByteBuf;
import targoss.hardcorealchemy.creatures.instinct.api.IInstinctNeed;

/**
 * Allows for custom syncing of Instinct Need data without
 * regards to how and where the Instinct Need is stored.
 * 
 * The implementation can be either stateless, or a unique instance
 * for the current need object.
 */
public interface INeedMessenger<T extends IInstinctNeed> {
    public boolean shouldSync();
    /**
     * Called when shouldSync() returns true
     * and the instinct system is about to sync
     * the need data.
     */
    public void toBytes(T need, ByteBuf buf);
    public void fromBytes(T need, ByteBuf buf);
}
