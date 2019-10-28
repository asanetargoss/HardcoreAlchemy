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

package targoss.hardcorealchemy.instinct.network;

import io.netty.buffer.ByteBuf;
import targoss.hardcorealchemy.instinct.InstinctNeedSpawnEnvironment;
import targoss.hardcorealchemy.instinct.api.IInstinctNeed;
import targoss.hardcorealchemy.instinct.network.api.INeedMessenger;
import targoss.hardcorealchemy.util.Serialization;

public class NeedMessengerSpawnEnvironment implements INeedMessenger<InstinctNeedSpawnEnvironment> {
    protected boolean shouldSync = false;
    
    protected boolean feltAtHome = false;
    
    protected static final int DATA_SEND_COOLDOWN_TIME = 20 * 20;
    protected boolean shouldSyncLater = false;
    protected int dataSendCooldown = 0;
    
    public void serverTick(InstinctNeedSpawnEnvironment need) {
        if (shouldSyncLater ||
                feltAtHome != need.feelsAtHome ||
                need.atHomeTestPosNotQueued) {
            need.atHomeTestPosNotQueued = false;
            if (dataSendCooldown > 0) {
                shouldSyncLater = true;
            } else  {
                shouldSyncLater = false;
                shouldSync = true;
                dataSendCooldown = DATA_SEND_COOLDOWN_TIME;
            }
        }
        
        if (dataSendCooldown > 0) {
            dataSendCooldown--;
        }
        feltAtHome = need.feelsAtHome;
    }

    @Override
    public boolean shouldSync() {
        return shouldSync;
    }

    @Override
    public void toBytes(InstinctNeedSpawnEnvironment need, ByteBuf buf) {
        buf.writeBoolean(need.feelsAtHome);
        buf.writeInt(need.atHomeStreak);
        buf.writeInt(need.maxAtHomeStreak);
        buf.writeFloat(need.averageAtHomeFraction);
        buf.writeFloat(need.preferredAtHomeFraction);
        
        boolean hasTestPos = need.atHomeTestPos != null;
        buf.writeBoolean(hasTestPos);
        if (hasTestPos) {
            Serialization.writeBlockPosToBuf(buf, need.atHomeTestPos);
        }
        
        shouldSync = false;
    }
    @Override
    public void fromBytes(InstinctNeedSpawnEnvironment need, ByteBuf buf) {
        need.feelsAtHome = buf.readBoolean();
        need.atHomeStreak = buf.readInt();
        need.maxAtHomeStreak = buf.readInt();
        need.averageAtHomeFraction = buf.readFloat();
        need.preferredAtHomeFraction = buf.readFloat();
        
        boolean hasTestPos = buf.readBoolean();
        if (hasTestPos) {
            need.atHomeTestPos = Serialization.readBlockPosFromBuf(buf);
        } else {
            need.atHomeTestPos = null;
        }
    }
}
