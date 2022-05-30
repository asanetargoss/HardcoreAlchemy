/*
 * Copyright 2017-2022 asanetargoss
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

package targoss.hardcorealchemy.creatures.instinct.internal;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import targoss.hardcorealchemy.config.Configs;
import targoss.hardcorealchemy.creatures.capability.instinct.ICapabilityInstinct;
import targoss.hardcorealchemy.creatures.instinct.api.IInstinctState;
import targoss.hardcorealchemy.creatures.instinct.api.InstinctEffect;
import targoss.hardcorealchemy.creatures.instinct.network.api.INeedMessenger;

public class InstinctState implements IInstinctState {
    public InstinctState() {}
    
    public EntityPlayer player = null;
    // Updated each tick by InstinctSystem
    public float instinct = ICapabilityInstinct.DEFAULT_INSTINCT_VALUE;
    public NeedStatus needStatus = NeedStatus.NONE;
    // A historic value checked and reset by the instinct system (ListenerPlayerInstinct)
    public NeedStatus lastNeedStatus = NeedStatus.NONE;
    public Map<InstinctEffect, Float> effectAmplifiers = new HashMap<>();
    public boolean shouldSyncNeed = false;
    public INeedMessenger messenger = IInstinctState.DEFAULT_MESSENGER;

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
    
    public static float getInstinctChangePerTick(Configs configs, NeedStatus needStatus) {
        if (configs.base.fastInstinctDecay && needStatus == NeedStatus.EVENTUALLY) {
            needStatus = NeedStatus.URGENT;
        }
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
    
    public float getInstinctChangePerTick(Configs configs) {
        return InstinctState.getInstinctChangePerTick(configs, this.needStatus);
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
    public INeedMessenger getNeedMessenger() {
        return messenger;
    }
}
