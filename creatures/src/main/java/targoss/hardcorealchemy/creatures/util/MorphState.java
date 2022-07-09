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

package targoss.hardcorealchemy.creatures.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import mchorse.metamorph.api.EntityUtils;
import mchorse.metamorph.api.MorphAPI;
import mchorse.metamorph.api.MorphManager;
import mchorse.metamorph.api.abilities.IAbility;
import mchorse.metamorph.api.morphs.AbstractMorph;
import mchorse.metamorph.api.morphs.EntityMorph;
import mchorse.metamorph.capabilities.morphing.IMorphing;
import mchorse.metamorph.capabilities.morphing.Morphing;
import mchorse.metamorph.network.Dispatcher;
import mchorse.metamorph.network.common.survival.PacketRemoveMorph;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import targoss.hardcorealchemy.capability.humanity.ICapabilityHumanity;
import targoss.hardcorealchemy.capability.humanity.LostMorphReason;
import targoss.hardcorealchemy.config.Configs;
import targoss.hardcorealchemy.creatures.HardcoreAlchemyCreatures;
import targoss.hardcorealchemy.creatures.capability.instinct.ICapabilityInstinct;
import targoss.hardcorealchemy.creatures.instinct.Instincts;
import targoss.hardcorealchemy.creatures.instinct.api.Instinct;
import targoss.hardcorealchemy.creatures.listener.ListenerPlayerHumanity;
import targoss.hardcorealchemy.creatures.listener.ListenerPlayerKillMastery;
import targoss.hardcorealchemy.creatures.listener.ListenerPlayerMorphs;
import targoss.hardcorealchemy.creatures.network.MessageForceForm;
import targoss.hardcorealchemy.event.EventPlayerMorphStateChange;

public class MorphState {
    @CapabilityInject(ICapabilityHumanity.class)
    public static final Capability<ICapabilityHumanity> HUMANITY_CAPABILITY = null;
    
    @CapabilityInject(ICapabilityInstinct.class)
    public static final Capability<ICapabilityInstinct> INSTINCT_CAPABILITY = null;

    public static float[] PLAYER_WIDTH_HEIGHT = new float[]{ 0.6F, 1.8F };
    
    public static AbstractMorph createMorph(String morphName) {
        return createMorph(morphName, new NBTTagCompound());
    }
    
    public static AbstractMorph createMorph(Entity entity) {
        NBTTagCompound nbt = new NBTTagCompound();
        String entityString = MorphManager.INSTANCE.morphNameFromEntity(entity);
        if (entityString == null) {
            return null;
        }
        nbt.setTag("EntityData", EntityUtils.stripEntityNBT(entity.serializeNBT()));
        return createMorph(entityString, nbt);
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
            Entity entity) {
        return forceForm(configs, player, reason, createMorph(entity));
    }

    public static boolean forceForm(Configs configs, EntityPlayer player, LostMorphReason reason,
            String morphName, NBTTagCompound morphProperties) {
        return forceForm(configs, player, reason, createMorph(morphName, morphProperties));
    }
    
    /**
     * Forces the player into the given AbstractMorph.
     * with the given reason, and updates the player's needs and instincts.
     * Returns true if successful
     * Note that like MorphAPI.morph, this function should generally only be called
     * on the server side, or you will get desyncs.
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
        
        AbstractMorph previousMorph = morphing.getCurrentMorph();
        
        boolean wasPlayer = previousMorph == null;
        boolean success = player.world.isRemote || MorphAPI.morph(player, morph, true);
        
        if (success) {
            if (reason != LostMorphReason.FORGOT_FORM || wasPlayer) {
                capabilityHumanity.loseMorphAbilityFor(reason);
            }
            if (!player.world.isRemote && reason == LostMorphReason.FORGOT_FORM && !wasPlayer && previousMorph != null) {
                int morphIndex = morphing.getAcquiredMorphs().indexOf(previousMorph);
                if (morphIndex != -1) {
                    morphing.remove(morphIndex);
                    Dispatcher.sendTo(new PacketRemoveMorph(morphIndex), (EntityPlayerMP)player);
                }
            }
            if (reason == LostMorphReason.FORGOT_FORM) {
                ListenerPlayerKillMastery.recalculateMasteredKills(player);
            }
            ListenerPlayerMorphs.updateMaxHumanity(player, false);
            
            // TODO: Why do players that still have humanity get instincts here?
            ICapabilityInstinct instincts = player.getCapability(INSTINCT_CAPABILITY, null);
            if (instincts != null) {
                instincts.clearInstincts(player);
                instincts.setInstinct(ICapabilityInstinct.DEFAULT_INSTINCT_VALUE);
                if (configs.base.enableInstincts && morph instanceof EntityMorph) {
                    MorphState.buildInstincts(player, instincts, ((EntityMorph)morph).getEntity(player.world));
                }
            }
            
            // TODO: Actually use this event for more things. Augment with the previous morph
            MinecraftForge.EVENT_BUS.post(new EventPlayerMorphStateChange.Post(player));
            
            if (!player.world.isRemote) {
                HardcoreAlchemyCreatures.proxy.messenger.sendTo(new MessageForceForm(reason, morph), (EntityPlayerMP)player);
            }
        }
        
        return success;
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
        
        for (IAbility playerAbility : morph.getSettings().abilities) {
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
