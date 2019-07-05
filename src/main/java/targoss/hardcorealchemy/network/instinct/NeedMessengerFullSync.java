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
import net.minecraftforge.fml.common.network.ByteBufUtils;
import targoss.hardcorealchemy.instinct.api.IInstinctNeed;

public class NeedMessengerFullSync implements INeedMessenger<IInstinctNeed> {
    protected boolean sync = false;
    
    public void sync() {
        sync = true;
    }

    @Override
    public boolean shouldSync() {
        return sync;
    }

    @Override
    public void toBytes(IInstinctNeed need, ByteBuf buf) {
        ByteBufUtils.writeTag(buf, need.serializeNBT());
    }

    @Override
    public void fromBytes(IInstinctNeed need, ByteBuf buf) {
        need.deserializeNBT(ByteBufUtils.readTag(buf));
    }
}
