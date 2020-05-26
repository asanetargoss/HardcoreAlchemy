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

package targoss.hardcorealchemy.instinct.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import targoss.hardcorealchemy.capability.instinct.ICapabilityInstinct;
import targoss.hardcorealchemy.config.Configs;
import targoss.hardcorealchemy.instinct.api.IInstinctState;
import targoss.hardcorealchemy.instinct.api.InstinctEffect;
import targoss.hardcorealchemy.network.MessageInstinctEffects;
import targoss.hardcorealchemy.network.MessageInstinctNeedChanged;
import targoss.hardcorealchemy.network.MessageInstinctNeedState;
import targoss.hardcorealchemy.network.PacketHandler;
import targoss.hardcorealchemy.util.Chat;
import targoss.hardcorealchemy.util.MorphState;

/**
 * The InstinctSystem is responsible for updating the internals of
 * a player's instincts.
 * 
 * This handles things like:
 * - Updating instinct values
 * - Enabling/disabling instinct effects
 * - Sending network packets related to instincts
 */
public class InstinctSystem {
    @CapabilityInject(ICapabilityInstinct.class)
    public static final Capability<ICapabilityInstinct> INSTINCT_CAPABILITY = null;
    /** Fractional amount of instinct lost per tick. One instinct icon lasts 2 days */
    public static final double INSTINCT_LOSS_RATE = 2.0D/24000.0D/2.0D;
    /** If an instinct check fails, the amount of ticks that must pass before another check can occur */
    public static final int INSTINCT_CHECK_INTERVAL = 24000 / 4;
    /** The time in ticks that must pass before a message is displayed that
     * a need became worse. Reset when the instinct bar is full */
    public static final int NEED_CHANGE_MESSAGE_MIN_TIME = 200;
    
    protected Configs configs;
    
    public InstinctSystem(Configs configs) {
        this.configs = configs;
    }
    
    public static void updateInstinct(EntityPlayer player, ICapabilityInstinct instinctCapability) {
        float maxInstinct;
        IAttributeInstance maxInstinctAttribute = player.getEntityAttribute(ICapabilityInstinct.MAX_INSTINCT);
        if (maxInstinctAttribute != null) {
            maxInstinct = (float)maxInstinctAttribute.getAttributeValue();
        }
        else {
            maxInstinct = (float)ICapabilityInstinct.MAX_INSTINCT.getDefaultValue();
        }
        float lowestNeedInstinct = maxInstinct;
        
        for (ICapabilityInstinct.InstinctEntry entry : instinctCapability.getInstincts()) {
            float entryInstinct = maxInstinct;
            
            for (InstinctNeedWrapper needWrapper : entry.getNeeds(player)) {
                InstinctState instinctState = needWrapper.getState(player);
                float instinct = instinctState.instinct;
                instinct += instinctState.getInstinctChangePerTick();
                instinct = MathHelper.clamp(instinct, 0.0F, maxInstinct);
                
                instinctState.instinct = instinct;
                
                entryInstinct = Math.min(entryInstinct, instinct);
            }
            entry.instinctValue = entryInstinct;
            
            lowestNeedInstinct = Math.min(lowestNeedInstinct, entryInstinct);
        }
        
        instinctCapability.setInstinct(lowestNeedInstinct);
    }
    
    /**
     * Displays a random instinct need in chat.
     * If necessary, randomly iterate through the needs
     * until one of them gives a non-null need message.
     */
    public static void sendRandomInstinctToChat(EntityPlayerMP player, ICapabilityInstinct instinctCap) {
        List<InstinctNeedWrapper> allNeeds = new ArrayList<>();
        for (ICapabilityInstinct.InstinctEntry entry : instinctCap.getInstincts()) {
            allNeeds.addAll(entry.getNeeds(player));
        }
        Collections.shuffle(allNeeds);
        
        for (InstinctNeedWrapper need : allNeeds) {
            ITextComponent needMessage = need.getNeed(player).getNeedMessage(need.state.needStatus);
            if (needMessage != null) {
                Chat.message(Chat.Type.NOTIFY, player, needMessage);
                break;
            }
        }
    }

    public static void displayNeedMessages(EntityPlayerMP player, ICapabilityInstinct instinct) {
        // Display a periodic need message about what one of the needs does
        int instinctMessageTime = instinct.getInstinctMessageTime();
        if (instinctMessageTime >= INSTINCT_CHECK_INTERVAL) {
            sendRandomInstinctToChat(player, instinct);
            instinctMessageTime = 0;
        }
        else {
            instinctMessageTime++;
        }
        instinct.setInstinctMessageTime(instinctMessageTime);
        
        // If a need becomes more urgent, give an opportunity to display a message about the need
        float maxInstinct = (float)ICapabilityInstinct.MAX_INSTINCT.getDefaultValue();
        IAttributeInstance maxInstinctAttribute = player.getEntityAttribute(ICapabilityInstinct.MAX_INSTINCT);
        if (maxInstinctAttribute != null) {
            maxInstinct = (float)maxInstinctAttribute.getAttributeValue();
        }
        if (instinct.getInstinct() >= maxInstinct) {
            // The player's instinct bar is full. Reset things so need messages can be displayed again.
            for (ICapabilityInstinct.InstinctEntry entry : instinct.getInstincts()) {
                for (InstinctNeedWrapper needWrapper : entry.getNeeds(player)) {
                    needWrapper.mostSevereStatusSinceMessage = InstinctState.NeedStatus.NONE;
                    needWrapper.playerTickSinceInstinctFull = player.ticksExisted;
                }
            }
        }
        else {
            // For each need, check if the need status has changed, and if so, allow it to display a message.
            for (ICapabilityInstinct.InstinctEntry entry : instinct.getInstincts()) {
                for (InstinctNeedWrapper needWrapper : entry.getNeeds(player)) {
                    if (needWrapper.playerTickSinceInstinctFull > player.ticksExisted) {
                        // This doesn't make sense
                        needWrapper.playerTickSinceInstinctFull = player.ticksExisted;
                    }
                    if (player.ticksExisted - needWrapper.playerTickSinceInstinctFull < NEED_CHANGE_MESSAGE_MIN_TIME) {
                        // Too early; throttle messages
                        return;
                    }
                    if (needWrapper.state.needStatus.ordinal() <= needWrapper.mostSevereStatusSinceMessage.ordinal()) {
                        // Not more severe since the instinct bar was full
                        return;
                    }
                    
                    needWrapper.mostSevereStatusSinceMessage = needWrapper.state.needStatus;
                    needWrapper.playerTickSinceInstinctFull = player.ticksExisted;
                    ITextComponent needMessage = needWrapper.getNeed(player).getNeedUnfulfilledMessage(needWrapper.state.needStatus);
                    if (needMessage != null) {
                        Chat.message(Chat.Type.NOTIFY, player, needMessage);
                    }
                }
            }
        }
    }
    
    public static Map<InstinctEffect, InstinctEffectWrapper> computeNewActiveEffects(EntityPlayer player, ICapabilityInstinct instinct) {
        Map<InstinctEffect, InstinctEffectWrapper> pastEffects = instinct.getActiveEffects();
        Map<InstinctEffect, InstinctEffectWrapper> newEffects = new HashMap<>();

        for (ICapabilityInstinct.InstinctEntry entry : instinct.getInstincts()) {
            // First, see if any needs in this instinct are not being met
            // If all needs are satisfied, new effects will not be activated
            // Note that existing effects will remain active with the
            //  current amplifier or higher unless instinct exceeds the maxInstinct of all candidate effects of the same type
            boolean needsMet = true;
            for (InstinctNeedWrapper needWrapper : entry.getNeeds(player)) {
                needsMet &= needWrapper.state.needStatus == IInstinctState.NeedStatus.NONE;
            }
            
            // Then find new effects amplitudes
            Map<InstinctEffect, Float> effectAmplifiers = new HashMap<>();
            for (InstinctNeedWrapper needWrapper : entry.getNeeds(player)) {
                // Instinct needs may choose to amplify existing instinct effects
                //NOTE: This feature is currently unused. Maybe it should be removed? IInstinctEffectData is more flexible and does less unneeded work.
                for (Map.Entry<InstinctEffect, Float> amplifierEntry : needWrapper.state.effectAmplifiers.entrySet()) {
                    InstinctEffect effect = amplifierEntry.getKey();
                    Float amplifier = effectAmplifiers.get(effect);
                    if (amplifier == null) {
                        amplifier = amplifierEntry.getValue();
                    }
                    else {
                        amplifier = Math.max(amplifier, amplifierEntry.getValue());
                    }
                    effectAmplifiers.put(effect, amplifier);
                }
            }
            
            // Then, see if we actually want to apply each effect
        checkEffect:
            for (InstinctEffectWrapper effectWrapper : entry.getEffects(player)) {
                if (entry.instinctValue > effectWrapper.maxInstinct) {
                    // Instinct is too high
                    continue;
                }
                
                InstinctEffect effect = effectWrapper.effect;
                
                // First check if the need should be activated
                InstinctEffectWrapper pastEffect = pastEffects.get(effect);
                if (pastEffect == null) {
                    // Do not activate if all needs in this instinct are being met
                    if (needsMet) {
                        continue checkEffect;
                    }
                    // Do not activate if one of the needs doesn't want it
                    for (InstinctNeedWrapper needWrapper : entry.getNeeds(player)) {
                        if (!needWrapper.getNeed(player).shouldActivateEffect(needWrapper.getState(player), effect)) {
                            continue checkEffect;
                        }
                    }
                }
                
                // The effect to be added/amplified
                InstinctEffectWrapper newEffect = newEffects.get(effect);
                if (newEffect == null) {
                    newEffect = new InstinctEffectWrapper(effectWrapper);
                    newEffects.put(effect, newEffect);
                }
                else {
                    // There are multiple effects of this type. Combine them.
                    newEffect.combine(effectWrapper);
                }
                
                if (pastEffect != null) {
                    // The effect already is applied. Keep its amplifier.
                    newEffect.combine(pastEffect);
                }
                
                Float newAmplifier = effectAmplifiers.get(effect);
                if (newAmplifier != null) {
                    // A need has requested for this effect to be amplified
                    newEffect.amplify(newAmplifier);
                    // The amplifier has been applied, so don't use it again.
                    effectAmplifiers.remove(effect);
                    for (InstinctNeedWrapper needWrapper : entry.getNeeds(player)) {
                        needWrapper.state.effectAmplifiers.remove(effect);
                    }
                }
            }
        }

        // Forced effects
        /* A maxInstinct of -infinity makes the effect go away immediately when
         * the forced effect is no longer in place UNLESS
         * there is another non-forced effect that has already
         * been applied.
         */
        for (ICapabilityInstinct.ForcedEffectEntry forcedEffectEntry : instinct.getForcedEffects().getInternalList()) {
            if (forcedEffectEntry == null) {
                continue;
            }

            InstinctEffect forcedEffect = forcedEffectEntry.effect;
            float forcedAmplitude = forcedEffectEntry.amplitude;
            InstinctEffectWrapper newEffect = newEffects.get(forcedEffect);
            if (newEffect == null) {
                newEffect = new InstinctEffectWrapper();
                newEffect.effect = forcedEffect;
                newEffect.amplifier = forcedAmplitude;
                newEffect.maxInstinct = Float.NEGATIVE_INFINITY;
                newEffects.put(forcedEffect, newEffect);
            }
            else {
                // There are multiple effects of this type. Combine them.
                newEffect.amplify(forcedAmplitude);
            }
        }
        
        return newEffects;
    }
    
    /**
     * Helper function for differential instinct effect state (useful for efficient networking).
     * If an instinct effect is in the map, either it has been added or its amplifier has changed.
     * If the stored value is null, that effect has been deactivated.
     */
    public static Map<InstinctEffect, InstinctEffectWrapper> getEffectChanges(Map<InstinctEffect, InstinctEffectWrapper> oldEffects,
            Map<InstinctEffect, InstinctEffectWrapper> newEffects) {
        Map<InstinctEffect, InstinctEffectWrapper> changes = new HashMap<>();
        
        for (InstinctEffect oldEffect : oldEffects.keySet()) {
            changes.put(oldEffect, null);
        }
        for (InstinctEffectWrapper newEffectWrapper : newEffects.values()) {
            InstinctEffect effect = newEffectWrapper.effect;
            if (changes.get(effect) == null ||
                    oldEffects.get(effect).amplifier != newEffectWrapper.amplifier) {
                changes.put(effect, newEffectWrapper);
            }
        }
        
        return changes;
    }

    /**
     * Given a list of effects which may have changed, call activate/deactivate
     * when necessary and then update the list of active effects.
     * 
     * See ListenerPlayerInstinct.getEffectChanges
     */
    public static void transitionEffects(EntityPlayer player,
            ICapabilityInstinct instinct,
            Map<InstinctEffect, InstinctEffectWrapper> effectChanges) {
        Map<InstinctEffect, InstinctEffectWrapper> pastEffects = instinct.getActiveEffects();
        Map<InstinctEffect, InstinctEffectWrapper> newEffects = new HashMap<>();
        newEffects.putAll(pastEffects);
        
        for (Map.Entry<InstinctEffect, InstinctEffectWrapper> changes : effectChanges.entrySet()) {
            InstinctEffect effect = changes.getKey();
            InstinctEffectWrapper wrapper = changes.getValue();
            InstinctEffectWrapper pastWrapper = pastEffects.get(effect);
            if (wrapper == null) {
                if (pastWrapper != null) {
                    pastWrapper.effect.onDeactivate(player, pastWrapper.amplifier);
                    newEffects.remove(effect);
                }
            }
            else {
                if (pastWrapper == null) {
                    effect.onActivate(player, wrapper.amplifier);
                    newEffects.put(effect, wrapper);
                }
                else {
                    if (pastWrapper.amplifier != wrapper.amplifier) {
                        effect.onDeactivate(player, pastWrapper.amplifier);
                        effect.onActivate(player, wrapper.amplifier);
                        newEffects.put(effect, wrapper);
                    }
                }
            }
        }
        
        // Finally, update active effects
        instinct.setActiveEffects(newEffects);
    }
    
    public void tickPlayer(ICapabilityInstinct instinct, PlayerTickEvent event) {
        EntityPlayer player = event.player;
        
        // Rather than bail out at the beginning, check if some instinct effects are enabled, as they may alter state.
        if (!configs.base.enableInstincts) {
            if (instinct.getEnabled()) {
                instinct.clearInstincts(player);
                instinct.setInstinct(ICapabilityInstinct.DEFAULT_INSTINCT_VALUE);
                instinct.setEnabled(false);
            }
            return;
        } else {
            if (!instinct.getEnabled()) {
                // Need to re-initialize
                MorphState.buildInstincts(player, instinct);
                instinct.setEnabled(true);
            }
        }

        List<ICapabilityInstinct.InstinctEntry> entries = instinct.getInstincts();
        if (entries.size() == 0 && instinct.getActiveEffects().size() == 0) {
            return;
        }
        
        // Check needs
        
        // Tick each need, get the associated instinct change, and sync if needed
        boolean needStatusChanged = false;
        int entryCount = 0;
        for (ICapabilityInstinct.InstinctEntry entry : instinct.getInstincts()) {
            int needCountPerEntry = 0;
            for (InstinctNeedWrapper needWrapper : entry.getNeeds(player)) {
                InstinctState instinctState = needWrapper.getState(player);
                
                needStatusChanged |= instinctState.needStatus != instinctState.lastNeedStatus;
                instinctState.lastNeedStatus = instinctState.needStatus;
                
                if (!player.world.isRemote && instinctState.messenger.shouldSync()) {
                    PacketHandler.INSTANCE.sendTo(new MessageInstinctNeedChanged(entryCount, needCountPerEntry, needWrapper), (EntityPlayerMP)player);
                    instinctState.shouldSyncNeed = false;
                }
                needCountPerEntry++;
            }
            entryCount++;
        }
        if (needStatusChanged && !player.world.isRemote) {
            PacketHandler.INSTANCE.sendTo(new MessageInstinctNeedState(instinct), (EntityPlayerMP)player);
        }
        
        // Display messages that tell the player information about the instincts that affect them
        if (!player.world.isRemote) {
            displayNeedMessages((EntityPlayerMP)player, instinct);
        }
        
        // Update the active effects, calling activation/deactivation functions as necessary.
        // Effect changes are handled differentially to make efficient network syncing easy.
        Map<InstinctEffect, InstinctEffectWrapper> oldEffects = instinct.getActiveEffects();
        Map<InstinctEffect, InstinctEffectWrapper> newEffects = computeNewActiveEffects(player, instinct);
        Map<InstinctEffect, InstinctEffectWrapper> effectChanges = getEffectChanges(oldEffects, newEffects);
        transitionEffects(player, instinct, effectChanges);
        if (!player.world.isRemote && effectChanges.size() != 0) {
            PacketHandler.INSTANCE.sendTo(new MessageInstinctEffects(effectChanges), (EntityPlayerMP)player);
        }
        
        // Finally, given all of the above, change the instinct value of the player
        updateInstinct(player, instinct);
    }
}
