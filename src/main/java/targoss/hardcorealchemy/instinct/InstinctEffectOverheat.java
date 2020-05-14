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

import java.util.Random;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import targoss.hardcorealchemy.capability.instinct.ICapabilityInstinct;
import targoss.hardcorealchemy.instinct.api.IInstinctEffectData;
import targoss.hardcorealchemy.instinct.api.InstinctEffect;

/** Exposure to flame causes player to heat/burn things */
public class InstinctEffectOverheat extends InstinctEffect {
    @CapabilityInject(ICapabilityInstinct.class)
    private static final Capability<ICapabilityInstinct> INSTINCT_CAPABILITY = null;

    // Two days? Sure, why not? What's the worst that could happen?
    public static final float OVERHEAT_TIME_PER_AMPLIFIER = 5 * 12000;
    public static final int OVERHEAT_EVENT_FREQUENCY = 1 / 3 * 12000;
    public static final float OVERHEAT_EVENT_CHANCE = 0.333F;

    protected static class Data implements IInstinctEffectData {
        public Random random = new Random();
        /** If >0 then morph is overheating (measured in ticks) */
        public int overheatTimer;
        public int maxOverheatTimer;
        public int nextOverheatEventTime;
        
        public void updateTimers() {
            if (overheatTimer > 0) {
                --overheatTimer;
                overheatTimer = Math.min(overheatTimer, maxOverheatTimer);
                ++nextOverheatEventTime;
            }
            else {
                nextOverheatEventTime = 0;
            }
        }
        
        public static final String NBT_OVERHEAT_TIMER = "overheat";
        public static final String NBT_MAX_OVERHEAT_TIMER = "max_overheat";
        public static final String NBT_NEXT_OVERHEAT_TIME = "event_time";

        @Override
        public NBTTagCompound serializeNBT() {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setInteger(NBT_OVERHEAT_TIMER, overheatTimer);
            nbt.setInteger(NBT_MAX_OVERHEAT_TIMER, maxOverheatTimer);
            nbt.setInteger(NBT_NEXT_OVERHEAT_TIME, nextOverheatEventTime);
            return nbt;
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            overheatTimer = nbt.getInteger(NBT_OVERHEAT_TIMER);
            maxOverheatTimer = nbt.getInteger(NBT_MAX_OVERHEAT_TIMER);
            nextOverheatEventTime = nbt.getInteger(NBT_NEXT_OVERHEAT_TIME);
        }
    }
    
    @Override
    public IInstinctEffectData createData() {
        return new Data();
    }

    public void exposeToHeat(EntityPlayer player, Data data) {
        // TODO: Message that the player did something unexpectedly bad
        data.overheatTimer = data.maxOverheatTimer;
    }
    
    public void doOverheatEvent(EntityPlayer player, float amplifier) {
        // TODO: These are random and occur semi-frequently
        // boilLiquidsInInventory(player)
        // burnItemsInInventory(player)
        // meltItemsInInventory(player)
        // setWorldAflame(player)
    }

    @Override
    public void onActivate(EntityPlayer player, float amplifier) {
        ICapabilityInstinct instinct = player.getCapability(INSTINCT_CAPABILITY, null);
        if (instinct == null) {
            return;
        }
        Data data = (Data)instinct.getInstinctEffectData(this);

        data.maxOverheatTimer = (int)(OVERHEAT_TIME_PER_AMPLIFIER * amplifier);
    }

    @Override
    public void onDeactivate(EntityPlayer player, float amplifier) {
        // TODO Auto-generated method stub

    }

    @Override
    public void tick(EntityPlayer player, float amplifier) {
        if (player.world.isRemote) {
            return;
        }

        ICapabilityInstinct instinct = player.getCapability(INSTINCT_CAPABILITY, null);
        if (instinct == null) {
            return;
        }
        Data data = (Data)instinct.getInstinctEffectData(this);

        if (InstinctEffectNetherFever.isInHeat(player)) {
            exposeToHeat(player, data);
        }
        if (data.overheatTimer > 0) {
            if (data.nextOverheatEventTime > OVERHEAT_EVENT_FREQUENCY &&
                    data.random.nextFloat() >= OVERHEAT_EVENT_CHANCE) {
                doOverheatEvent(player, amplifier);
            }
            // See also ListenerInstinctOverheat
        }
        data.updateTimers();
    }

}
