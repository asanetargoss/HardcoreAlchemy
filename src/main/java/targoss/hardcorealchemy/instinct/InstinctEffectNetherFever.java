/*
 * Copyright 2020 asanetargoss
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

package targoss.hardcorealchemy.instinct;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import targoss.hardcorealchemy.capability.instinct.ICapabilityInstinct;
import targoss.hardcorealchemy.instinct.api.IInstinctEffectData;
import targoss.hardcorealchemy.instinct.api.InstinctEffect;

public class InstinctEffectNetherFever extends InstinctEffect {
    @CapabilityInject(ICapabilityInstinct.class)
    private static final Capability<ICapabilityInstinct> INSTINCT_CAPABILITY = null;
    
    public static boolean isInHeat(EntityPlayer player) {
        // TODO: Check for player being in fire/in lava
        return false;
    }
    
    public static float toOverheatAmplifier(float amplifier) {
        if (amplifier < 1.0F) {
            return 0.0F;
        }
        return amplifier - 1.0F;
    }

    public static float toCoolingAmplifier(float amplifier) {
        return amplifier;
    }

    protected static class Data implements IInstinctEffectData {
        InstinctEffectOverheat overheatEffect = new InstinctEffectOverheat();
        InstinctEffectTemperedFlame coolingEffect = new InstinctEffectTemperedFlame();

        @Override
        public NBTTagCompound serializeNBT() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            // TODO Auto-generated method stub
            
        }
    }
    
    @Override
    public IInstinctEffectData createData() {
        return new Data();
    }

    @Override
    public void onActivate(EntityPlayer player, float amplifier) {
        ICapabilityInstinct instinct = player.getCapability(INSTINCT_CAPABILITY, null);
        if (instinct == null) {
            return;
        }
        Data data = (Data)instinct.getInstinctEffectData(this);

        data.overheatEffect.onActivate(player, toOverheatAmplifier(amplifier));
        data.coolingEffect.onActivate(player, toCoolingAmplifier(amplifier));
    }

    @Override
    public void onDeactivate(EntityPlayer player, float amplifier) {
        ICapabilityInstinct instinct = player.getCapability(INSTINCT_CAPABILITY, null);
        if (instinct == null) {
            return;
        }
        Data data = (Data)instinct.getInstinctEffectData(this);

        data.overheatEffect.onDeactivate(player, toOverheatAmplifier(amplifier));
        data.coolingEffect.onDeactivate(player, toCoolingAmplifier(amplifier));
    }

    @Override
    public void tick(EntityPlayer player, float amplifier) {
        ICapabilityInstinct instinct = player.getCapability(INSTINCT_CAPABILITY, null);
        if (instinct == null) {
            return;
        }
        Data data = (Data)instinct.getInstinctEffectData(this);

        data.overheatEffect.tick(player, toOverheatAmplifier(amplifier));
        data.coolingEffect.tick(player, toCoolingAmplifier(amplifier));
    }

}
