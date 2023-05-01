/*
 * Copyright 2017-2023 asanetargoss
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
import targoss.hardcorealchemy.capability.humanity.MorphAbilityChangeReason;
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
     * TODO: This function is too specialized. It should be removed
     */
    public static boolean resetForm(Configs configs, EntityPlayer player) {
        return forceForm(configs, player, MorphAbilityChangeReason.REGAINED_MORPH_ABILITY, (AbstractMorph)null);
    }
    
    public static boolean forceForm(Configs configs, EntityPlayer player, MorphAbilityChangeReason reason) {
        IMorphing morphing = player.getCapability(ListenerPlayerHumanity.MORPHING_CAPABILITY, null);
        AbstractMorph currentMorph = morphing == null ? null : morphing.getCurrentMorph();
        return forceForm(configs, player, reason, currentMorph);
    }
    
    public static boolean forceForm(Configs configs, EntityPlayer player, MorphAbilityChangeReason reason,
            String morphName) {
        return forceForm(configs, player, reason, createMorph(morphName));
    }
    
    public static boolean forceForm(Configs configs, EntityPlayer player, MorphAbilityChangeReason reason,
            Entity entity) {
        return forceForm(configs, player, reason, createMorph(entity));
    }

    public static boolean forceForm(Configs configs, EntityPlayer player, MorphAbilityChangeReason reason,
            String morphName, NBTTagCompound morphProperties) {
        return forceForm(configs, player, reason, createMorph(morphName, morphProperties));
    }
    

    public static boolean forceForm(Configs configs, EntityPlayer player, MorphAbilityChangeReason reason,
            @Nullable AbstractMorph morph) {
        if (player.world.isRemote) {
            throw new IllegalStateException("forceForm should only be called on the server to prevent desyncs");
        }
        IMorphing morphing = player.getCapability(ListenerPlayerHumanity.MORPHING_CAPABILITY, null);
        if (morphing == null) {
            return false;
        }
        AbstractMorph lastMorph = morphing.getCurrentMorph();
        return forceForm(configs, player, reason, morphing, lastMorph, morph);
    }

    /**
     * Updates the player's morph abilities.
     * If the morph is different than the current one, morph the player into the given AbstractMorph,
     * with the given reason, and updates the player's needs and instincts.
     * Returns true if successful.
     * Note that like MorphAPI.morph, this function should generally only be called
     * on the server side, or you will get desyncs.
     */
    public static boolean forceForm(Configs configs, EntityPlayer player, MorphAbilityChangeReason reason,
            IMorphing morphing, @Nullable AbstractMorph lastMorph, @Nullable AbstractMorph morph) {
        ICapabilityHumanity humanity = player.getCapability(HUMANITY_CAPABILITY, null);
        if (humanity == null) {
            return false;
        }
        if (reason == MorphAbilityChangeReason.FORGOT_LAST_FORM && lastMorph == null) {
            reason = MorphAbilityChangeReason.FORGOT_HUMAN_FORM;
        }

        // NOTE: On the client side, the player is already morphed because MorphAPI.morph sent a packet first.
        // TODO: Better morph equality comparison
        boolean sameMorph = (lastMorph == null && morph == null) ||
                            (lastMorph != null && lastMorph.equals(morph));
        boolean success = player.world.isRemote ||
                          sameMorph ||
                          MorphAPI.morph(player, morph, true);
        
        if (success) {
            boolean couldMorph = humanity.canMorph();
            humanity.changeMorphAbilityFor(reason);
            if (reason == MorphAbilityChangeReason.FORGOT_LAST_FORM) {
                if (lastMorph != null) {
                    int morphIndex = morphing.getAcquiredMorphs().indexOf(lastMorph);
                    if (morphIndex != -1) {
                        morphing.remove(morphIndex);
                    }
                }
                ListenerPlayerKillMastery.recalculateMasteredKills(player);
            }
            ListenerPlayerMorphs.updateMaxHumanity(player, false);
            
            if (!sameMorph || (couldMorph != humanity.canMorph())) {
                ICapabilityInstinct instincts = player.getCapability(INSTINCT_CAPABILITY, null);
                MorphState.buildInstincts(configs, player, instincts, humanity, morph);
            }
            
            // TODO: Actually use this event for more things. Include the previous morph and humanity state in the event constructor
            MinecraftForge.EVENT_BUS.post(new EventPlayerMorphStateChange.Post(player));
            
            if (!player.world.isRemote) {
                HardcoreAlchemyCreatures.proxy.messenger.sendTo(new MessageForceForm(reason, lastMorph, morph), (EntityPlayerMP)player);
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

    public static void buildInstincts(Configs configs, EntityPlayer player, ICapabilityInstinct instincts, ICapabilityHumanity humanity, AbstractMorph morph) {
        instincts.clearInstincts(player);
        instincts.setInstinct(ICapabilityInstinct.DEFAULT_INSTINCT_VALUE);
        
        if (!configs.base.enableInstincts) {
            return;
        }
        if (humanity == null || humanity.canMorph()) {
            return;
        }
        
        if (!(morph instanceof EntityMorph)) {
            return;
        }
        Entity morphEntity = ((EntityMorph)morph).getEntity(player.world);
        if (!(morphEntity instanceof EntityLiving)) {
            return;
        }
        EntityLiving morphEntityLiving = (EntityLiving)morphEntity;
        
        for (Instinct instinct : Instincts.REGISTRY.getValues()) {
            // No caching (for now); just go through the list of registered instincts and figure out which are applicable
            if (instinct.doesMorphEntityHaveInstinct(morphEntityLiving)) {
                instincts.addInstinct(instinct);
            }
        }
    }
    
    public static void buildInstincts(Configs configs, EntityPlayer player, ICapabilityInstinct instincts, ICapabilityHumanity humanity) {
        if (instincts == null) {
            return;
        }
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
        buildInstincts(configs, player, instincts, humanity, morph);
    }
    
    public static void buildInstincts(Configs configs, EntityPlayer player, ICapabilityInstinct instincts) {
        ICapabilityHumanity humanity = player.getCapability(HUMANITY_CAPABILITY, null);
        buildInstincts(configs, player, instincts, humanity);
    }
}
