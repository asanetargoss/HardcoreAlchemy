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
import net.minecraft.world.DimensionType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import targoss.hardcorealchemy.capability.instinct.ICapabilityInstinct;
import targoss.hardcorealchemy.instinct.api.IInstinctEffectData;
import targoss.hardcorealchemy.instinct.api.InstinctEffect;
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
                    WorldUtil.CollisionPredicate.FIRE,
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

    protected static class Data implements IInstinctEffectData {
        InstinctEffect overheatEffect = Instincts.EFFECT_OVERHEAT;
        InstinctEffect coolingEffect = Instincts.EFFECT_TEMPERED_FLAME;
        
        protected static final String NBT_OVERHEAT_EFFECT = "overheat";
        protected static final String NBG_COOLING_EFFECT = "cooling";
        
        /* Everything is stored in the child effects, which are managed by
         * the instinct capability. So, nothing has to be saved here.
         */

        @Override
        public NBTTagCompound serializeNBT() {
            return new NBTTagCompound();
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {}
    }
    
    @Override
    public IInstinctEffectData createData() {
        return new Data();
    }

    // TODO: Rather than call the effects directly, use the forced effect API in ICapabilityInstinct, and store the forced effect IDs.

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
