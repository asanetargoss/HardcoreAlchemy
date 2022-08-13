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

package targoss.hardcorealchemy.creatures.listener;

import static targoss.hardcorealchemy.item.Items.EMPTY_SLATE;

import java.util.List;
import java.util.Random;

import mchorse.metamorph.api.morphs.AbstractMorph;
import mchorse.metamorph.capabilities.morphing.IMorphing;
import mchorse.metamorph.capabilities.morphing.Morphing;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import targoss.hardcorealchemy.capability.humanity.ICapabilityHumanity;
import targoss.hardcorealchemy.capability.humanity.MorphAbilityChangeReason;
import targoss.hardcorealchemy.capability.humanity.ProviderHumanity;
import targoss.hardcorealchemy.creatures.item.ItemSealOfForm;
import targoss.hardcorealchemy.creatures.item.Items;
import targoss.hardcorealchemy.creatures.util.MorphState;
import targoss.hardcorealchemy.event.EventEnchant;
import targoss.hardcorealchemy.item.ItemEmptySlate;
import targoss.hardcorealchemy.listener.HardcoreAlchemyListener;
import targoss.hardcorealchemy.util.InventoryUtil;

public class ListenerPlayerSealOfForm extends HardcoreAlchemyListener {
    {
        ItemEmptySlate.enchantmentEnabled = true;
    }
    
    protected Random random = new Random();
    
    @SubscribeEvent
    public void onEnchantSeal(EventEnchant.Post event) {
        if (InventoryUtil.isEmptyItemStack(event.enchantStack)) {
            return;
        }
        if (event.enchantStack.getItem() != EMPTY_SLATE) {
            return;
        }
        EntityPlayer player = event.player;
        
        // Get the player's current form
        IMorphing morphing = Morphing.get(player);
        if (morphing == null) {
            return;
        }
        AbstractMorph sealMorph = morphing.getCurrentMorph();
        ItemStack sealOfFormStack = new ItemStack(Items.SEAL_OF_FORM);
        ItemSealOfForm.setMorphOnItem(sealOfFormStack, sealMorph);
        
        // Morph the player into some other form, depending on what they are morphed as and
        // what morphs are available.
        List<AbstractMorph> acquiredMorphs = morphing.getAcquiredMorphs();
        int availableFormCount = acquiredMorphs.size();
        ICapabilityHumanity humanity = player.getCapability(ProviderHumanity.HUMANITY_CAPABILITY, null);
        if (humanity == null || !humanity.getHasForgottenHumanForm()) {
            ++availableFormCount;
        }

        // Do some calculation to check if we have a "fallback" morph, i.e. a morph to go back to
        // (or the player if available).
        // Here, it's important to handle an edge case where the player's current form is not in
        // the acquired morphs list. In that case, the player has an extra form to fall back to.
        final int sealMorphIndex = acquiredMorphs.indexOf(sealMorph);
        final boolean hasFallbackMorph = availableFormCount >= 2 || (availableFormCount >= 1 && !((sealMorph == null && !humanity.getHasForgottenHumanForm()) || sealMorphIndex != -1));
        AbstractMorph newMorph = null;
        if (!hasFallbackMorph) {
            // If no morphs are available, morph the player into a slime.
            EntitySlime slime = new EntitySlime(player.world);
            slime.setSlimeSize(0);
            newMorph = MorphState.createMorph(slime);
        }
        else if (sealMorph == null || humanity.getHasForgottenHumanForm()) {
            // Check the player's morphing history. Morph the player into their most recent
            // morph if possible. This reduces unexpected consequences of morphing.
            // Use a try-catch because the upstream Metamorph may not have this function.
            AbstractMorph lastSelectedMorph;
            try {
                lastSelectedMorph = morphing.getLastSelectedMorph();
            } catch (Exception e) {
                lastSelectedMorph = null;
            }
            if (lastSelectedMorph != null && !lastSelectedMorph.equals(sealMorph) && acquiredMorphs.contains(lastSelectedMorph)) {
                newMorph = lastSelectedMorph;
            }
            if (newMorph == null) {
                int offset = sealMorphIndex;
                int n = acquiredMorphs.size();
                int range = n;
                if (offset != -1) {
                    --range;
                    ++offset;
                }
                else {
                    offset = 0;
                }
                int randomMorphIndexExcludingCurrent = (random.nextInt(range) + offset) % n;
                newMorph = acquiredMorphs.get(randomMorphIndexExcludingCurrent);
            }
        }
        else {
            newMorph = null;
        }
        
        if (availableFormCount > 0) {
            MorphState.forceForm(coreConfigs, player, MorphAbilityChangeReason.FORGOT_LAST_FORM, newMorph);
        }
        
        // Bind the player's form to the seal
        event.enchantStack = sealOfFormStack;
        
        if (availableFormCount == 0) {
            // What exactly did you expect to happen?
            player.attackEntityFrom(DamageSource.outOfWorld, 1000.0F);
        }
    }
}
