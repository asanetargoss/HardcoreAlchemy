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

package targoss.hardcorealchemy.creatures.instinct;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.DimensionType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import targoss.hardcorealchemy.creatures.capability.instinct.ICapabilityInstinct;
import targoss.hardcorealchemy.creatures.instinct.api.IInstinctEffectData;
import targoss.hardcorealchemy.creatures.instinct.api.InstinctEffect;
import targoss.hardcorealchemy.util.WorldUtil;

public class InstinctEffectNetherFever extends InstinctEffect {
    @CapabilityInject(ICapabilityInstinct.class)
    private static final Capability<ICapabilityInstinct> INSTINCT_CAPABILITY = null;
    
    public static boolean isInHeat(EntityPlayer player) {
        if (player.dimension != DimensionType.NETHER.getId()) {
            if (player.onGround) {
                boolean collidesWithFire = WorldUtil.doesCollide(player,
                        WorldUtil.CollisionPredicate.FIRE,
                        WorldUtil.CollisionMethod.FIRE);
                if (collidesWithFire) {
                    return true;
                }
            }

            boolean collidesWithLava = WorldUtil.doesCollide(player,
                    WorldUtil.CollisionPredicate.LAVA,
                    WorldUtil.CollisionMethod.LAVA);
            if (collidesWithLava) {
                return true;
            }
        }
        
        return false;
    }
    
    public static float toOverheatAmplifier(float amplifier) {
        if (amplifier < 1.0F) {
            return 0.0F;
        }
        return (amplifier - 1.0F) * 2.0F;
    }

    public static float toCoolingAmplifier(float amplifier) {
        return amplifier;
    }

    public static class Data implements IInstinctEffectData {
        // Just store the IDs of our forced effects
        public int overheatEffectID = -1;
        public int coolingEffectID = -1;
        
        protected static final String NBT_OVERHEAT_EFFECT_ID = "overheat";
        protected static final String NBT_COOLING_EFFECT_ID = "cooling";

        @Override
        public NBTTagCompound serializeNBT() {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setInteger(NBT_OVERHEAT_EFFECT_ID, overheatEffectID);
            nbt.setInteger(NBT_COOLING_EFFECT_ID, coolingEffectID);
            return nbt;
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            if (nbt.hasKey(NBT_OVERHEAT_EFFECT_ID)) {
                overheatEffectID = nbt.getInteger(NBT_OVERHEAT_EFFECT_ID);
            } else {
                overheatEffectID = -1;
            }
            if (nbt.hasKey(NBT_COOLING_EFFECT_ID)) {
                coolingEffectID = nbt.getInteger(NBT_COOLING_EFFECT_ID);
            } else {
                coolingEffectID = -1;
            }
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

        data.overheatEffectID = instinct.addForcedEffect(Instincts.EFFECT_OVERHEAT, toOverheatAmplifier(amplifier));
        data.coolingEffectID = instinct.addForcedEffect(Instincts.EFFECT_TEMPERED_FLAME, toCoolingAmplifier(amplifier));
        // The instinct system will sync the effects to the client automatically, so we don't really need to do anything else
    }

    @Override
    public void onDeactivate(EntityPlayer player, float amplifier) {
        ICapabilityInstinct instinct = player.getCapability(INSTINCT_CAPABILITY, null);
        if (instinct == null) {
            return;
        }
        Data data = (Data)instinct.getInstinctEffectData(this);

        instinct.removeForcedEffect(data.overheatEffectID, Instincts.EFFECT_OVERHEAT);
        data.overheatEffectID = -1;
        instinct.removeForcedEffect(data.coolingEffectID, Instincts.EFFECT_TEMPERED_FLAME);
        data.coolingEffectID = -1;
    }

    @Override
    public void tick(EntityPlayer player, float amplifier) {
        // All fancy behavior handled by the "child" forced effects
    }

}
