/*
 * Copyright 2018 asanetargoss
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

package targoss.hardcorealchemy.metamorph.action;

import mchorse.metamorph.api.abilities.IAction;
import mchorse.metamorph.api.morphs.AbstractMorph;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentTranslation;
import targoss.hardcorealchemy.capability.morphstate.ICapabilityMorphState;
import targoss.hardcorealchemy.capability.morphstate.ProviderMorphState;
import targoss.hardcorealchemy.util.Chat;

/**
 * Toggles a mode where the player can fish underwater
 * by chasing after fish swarms.
 */
public class UnderwaterFishing implements IAction {
    @Override
    public void execute(EntityLivingBase entity, AbstractMorph morph) {
        if (!(entity instanceof EntityPlayer)) {
            return;
        }
        EntityPlayer player = (EntityPlayer)entity;
        
        ICapabilityMorphState morphState = player.getCapability(ProviderMorphState.MORPH_STATE_CAPABILITY, null);
        if (morphState == null) {
            return;
        }
        
        // Toggle hunt state (cannot start hunt unless in water)
        if (morphState.getIsFishingUnderwater()) {
            morphState.setIsFishingUnderwater(false);
            Chat.messageSP(Chat.Type.NOTIFY, player, new TextComponentTranslation("hardcorealchemy.ability.fishing.endhunt"));
        }
        else {
            if (player.isInWater()) {
                morphState.setIsFishingUnderwater(true);
                Chat.messageSP(Chat.Type.NOTIFY, player, new TextComponentTranslation("hardcorealchemy.ability.fishing.beginhunt"));
            }
            else {
                Chat.messageSP(Chat.Type.WARN, player, new TextComponentTranslation("hardcorealchemy.ability.fishing.error.notinwater"));
            }
        }
    }

}
