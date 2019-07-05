/*
 * Copyright 2018 asanetargoss
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

package targoss.hardcorealchemy.instinct.api;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.capability.instinct.ICapabilityInstinct;
import targoss.hardcorealchemy.network.MessageInstinctNeedChanged;
import targoss.hardcorealchemy.network.PacketHandler;
import targoss.hardcorealchemy.network.instinct.INeedMessenger;
import targoss.hardcorealchemy.network.instinct.NeedMessengerFullSync;

public class InstinctState implements IInstinctState {
    public InstinctState() {}
    
    public static final INeedMessenger DEFAULT_MESSENGER = new NeedMessengerFullSync();
    
    public EntityPlayer player = null;
    public NeedStatus needStatus = NeedStatus.NONE;
    // A historic value checked and reset by the instinct system (ListenerPlayerInstinct)
    public NeedStatus lastNeedStatus = NeedStatus.NONE;
    public Map<InstinctEffect, Float> effectAmplifiers = new HashMap<>();
    public boolean shouldSyncNeed = false;
    public INeedMessenger messenger = DEFAULT_MESSENGER;

    @Override
    public EntityPlayer getPlayer() {
        return player;
    }

    @Override
    public void setNeedStatus(NeedStatus needStatus) {
        this.needStatus = needStatus;
    }
    
    private static final float MAX_INSTINCT = (float)ICapabilityInstinct.MAX_INSTINCT.getDefaultValue();
    private static final float DAYS_PER_TICK = 1.0F / (24000.0F);
    private static final float MINUTES_PER_TICK = 1.0F / (60.0F * 20.0F);
    
    public static float getInstinctChangePerTick(NeedStatus needStatus) {
        switch (needStatus) {
        case URGENT:
            return -1.0F * MAX_INSTINCT * MINUTES_PER_TICK / 3.0F;
        case EVENTUALLY:
            return -1.0F * MAX_INSTINCT * DAYS_PER_TICK / 20.0F;
        case WAITING:
            return 0.0F;
        case NONE:
        default:
            return 1.0F * MAX_INSTINCT * MINUTES_PER_TICK / 3.0F;
        }
    }
    
    public float getInstinctChangePerTick() {
        return InstinctState.getInstinctChangePerTick(this.needStatus);
    }

    @Override
    public void setEffectAmplifier(InstinctEffect instinctEffect, float amplifier) {
        Float currentAmplifier = effectAmplifiers.get(instinctEffect);
        if (currentAmplifier == null) {
            currentAmplifier = amplifier;
        }
        else {
            currentAmplifier = Math.max(currentAmplifier, amplifier);
        }
        effectAmplifiers.put(instinctEffect, amplifier);
    }
    
    @Override
    public void syncNeed() {
        if (player == null) {
            HardcoreAlchemy.LOGGER.warn("Received request to sync instinct need to client, but there is no player to send data to");
            return;
        }
        if (player.world.isRemote) {
            HardcoreAlchemy.LOGGER.warn("Received request to sync instinct need while on the client");
            return;
        }
        shouldSyncNeed = true;
    }
    
    @Override
    public INeedMessenger getNeedMessenger() {
        return messenger;
    }
}
