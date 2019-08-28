/*
 * Copyright 2017-2018 asanetargoss
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

package targoss.hardcorealchemy.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import ladysnake.dissolution.common.capabilities.IIncorporealHandler;
import mchorse.metamorph.api.MorphAPI;
import mchorse.metamorph.api.MorphManager;
import mchorse.metamorph.api.abilities.IAbility;
import mchorse.metamorph.api.morphs.AbstractMorph;
import mchorse.metamorph.api.morphs.EntityMorph;
import mchorse.metamorph.capabilities.morphing.IMorphing;
import mchorse.metamorph.capabilities.morphing.Morphing;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.common.Optional;
import targoss.hardcorealchemy.ModState;
import targoss.hardcorealchemy.capability.humanity.ICapabilityHumanity;
import targoss.hardcorealchemy.capability.humanity.LostMorphReason;
import targoss.hardcorealchemy.capability.instinct.ICapabilityInstinct;
import targoss.hardcorealchemy.config.Configs;
import targoss.hardcorealchemy.instinct.api.Instinct;
import targoss.hardcorealchemy.instinct.api.Instincts;
import targoss.hardcorealchemy.item.Items;
import targoss.hardcorealchemy.listener.ListenerPlayerDiet;
import targoss.hardcorealchemy.listener.ListenerPlayerHumanity;

public class MorphState {
    @CapabilityInject(ICapabilityHumanity.class)
    public static final Capability<ICapabilityHumanity> HUMANITY_CAPABILITY = null;
    
    @CapabilityInject(ICapabilityInstinct.class)
    public static final Capability<ICapabilityInstinct> INSTINCT_CAPABILITY = null;

    public static float[] PLAYER_WIDTH_HEIGHT = new float[]{ 0.6F, 1.8F };
    
    public static AbstractMorph createMorph(String morphName) {
        return createMorph(morphName, new NBTTagCompound());
    }
    
    public static AbstractMorph createMorph(String morphName, @Nonnull NBTTagCompound morphProperties) {
        morphProperties.setString("Name", morphName);
        return MorphManager.INSTANCE.morphFromNBT(morphProperties);
    }
    
    /**
     * Forces the player into human form, and clears the player's needs and instincts.
     * Returns true if successful
     */
    public static boolean resetForm(Configs configs, EntityPlayer player) {
        return forceForm(configs, player, LostMorphReason.REGAINED_MORPH_ABILITY, (AbstractMorph)null);
    }
    
    public static boolean forceForm(Configs configs, EntityPlayer player, LostMorphReason reason,
            String morphName) {
        return forceForm(configs, player, reason, createMorph(morphName));
    }

    public static boolean forceForm(Configs configs, EntityPlayer player, LostMorphReason reason,
            String morphName, NBTTagCompound morphProperties) {
        return forceForm(configs, player, reason, createMorph(morphName, morphProperties));
    }
    
    /*TODO: Consider making forceForm server-side authoritative, and
     * send special packets to the client, to avoid desyncs at a critical
     * state transition.
     */
    /**
     * Forces the player into the given AbstractMorph.
     * with the given reason, and updates the player's needs and instincts.
     * Returns true if successful
     */
    public static boolean forceForm(Configs configs, EntityPlayer player, LostMorphReason reason,
            @Nullable AbstractMorph morph) {
        IMorphing morphing = player.getCapability(ListenerPlayerHumanity.MORPHING_CAPABILITY, null);
        if (morphing == null) {
            return false;
        }
        ICapabilityHumanity capabilityHumanity = player.getCapability(HUMANITY_CAPABILITY, null);
        if (capabilityHumanity == null) {
            return false;
        }
        
        boolean success = true;
        
        AbstractMorph currentMorph = morphing.getCurrentMorph();
        if ((currentMorph == null && morph != null) ||
            (currentMorph != null && morph == null) ||
                (currentMorph != null && morph != null &&
                 !currentMorph.equals(morph))) {
            success = MorphAPI.morph(player, morph, true);
        }
        
        if (success) {
            capabilityHumanity.loseMorphAbilityFor(reason);
            
            double humanity;
            if (morph == null) {
                humanity = 20.0F;
            }
            else if (reason == LostMorphReason.REGAINED_MORPH_ABILITY) {
                humanity = capabilityHumanity.getHumanity();
            }
            else {
                humanity = 0.0F;
            }
            capabilityHumanity.setHumanity(humanity);
            
            if (reason != LostMorphReason.LOST_HUMANITY) {
                // Prevent showing the player a message that their humanity has changed
                capabilityHumanity.setLastHumanity(humanity);
            }
            
            if (ModState.isNutritionLoaded) {
                ListenerPlayerDiet.updateMorphDiet(player);
            }
            
            ICapabilityInstinct instincts = player.getCapability(INSTINCT_CAPABILITY, null);
            if (instincts != null) {
                instincts.clearInstincts(player);
                instincts.setInstinct(ICapabilityInstinct.DEFAULT_INSTINCT_VALUE);
                if (configs.base.enableInstincts && morph instanceof EntityMorph) {
                    MorphState.buildInstincts(player, instincts, ((EntityMorph)morph).getEntity(player.world));
                }
            }
        }
        
        return success;
    }
    
    //TODO: canMorph utility function
    
    public static boolean canUseHighMagic(EntityPlayer player) {
        IMorphing morphing = Morphing.get(player);
        if (morphing == null || morphing.getCurrentMorph() == null) {
            return true;
        }
        
        ICapabilityHumanity humanity = player.getCapability(HUMANITY_CAPABILITY, null);
        if (humanity == null || humanity.isHuman()) {
            return true;
        }
        
        if (player.getActivePotionEffect(Items.POTION_ALLOW_MAGIC) != null) {
            return true;
        }
        
        return false;
    }

    @Optional.Method(modid = ModState.DISSOLUTION_ID)
    public static boolean isIncorporeal(EntityPlayer player) {
        IIncorporealHandler incorporeal = player.getCapability(CapabilityIncorporealHandler.CAPABILITY_INCORPOREAL,
                null);
        if (incorporeal != null && incorporeal.isIncorporeal()) {
            return true;
        }
        return false;
    }
    
    public static boolean hasMorphAbility(EntityPlayer player, String abilityName) {
        IMorphing morphing = player.getCapability(ListenerPlayerHumanity.MORPHING_CAPABILITY, null);
        if (morphing == null) {
            return false;
        }
        AbstractMorph morph = morphing.getCurrentMorph();
        if (morph == null) {
            return false;
        }
        
        IAbility ability = MorphManager.INSTANCE.abilities.get(abilityName);
        if (ability == null) {
            return false;
        }
        
        for (IAbility playerAbility : morph.settings.abilities) {
            if (playerAbility == ability) {
                return true;
            }
        }
        return false;
    }

    public static void buildInstincts(EntityPlayer player, ICapabilityInstinct instincts, EntityLivingBase morphEntity) {
        instincts.clearInstincts(player);
        instincts.setInstinct(ICapabilityInstinct.DEFAULT_INSTINCT_VALUE);
        
        if (morphEntity == null || !(morphEntity instanceof EntityLiving)) {
            return;
        }
        EntityLiving morphedLiving = (EntityLiving)morphEntity;
        
        for (Instinct instinct : Instincts.REGISTRY.getValues()) {
            // No caching (for now); just go through the list of registered instincts and figure out which are applicable
            if (instinct.doesMorphEntityHaveInstinct(morphEntity)) {
                instincts.addInstinct(instinct);
            }
        }
    }
    
    public static void buildInstincts(EntityPlayer player, ICapabilityInstinct instincts) {
        instincts.clearInstincts(player);
        instincts.setInstinct(ICapabilityInstinct.DEFAULT_INSTINCT_VALUE);
        
        IMorphing morphing = Morphing.get(player);
        if (morphing == null) {
            return;
        }
        AbstractMorph morph = morphing.getCurrentMorph();
        if (!(morph instanceof EntityMorph)) {
            return;
        }
        EntityLivingBase morphEntity = ((EntityMorph)morph).getEntity(player.world);
        if (morphEntity == null) {
            return;
        }
        buildInstincts(player, instincts, morphEntity);
    }
}
