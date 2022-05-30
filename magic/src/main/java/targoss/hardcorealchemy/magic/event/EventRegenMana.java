/*
 * Copyright 2017-2022 asanetargoss
 *
 * This file is part of Hardcore Alchemy Magic.
 *
 * Hardcore Alchemy Magic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Magic is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Magic. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.magic.event;

import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;
import targoss.hardcorealchemy.coremod.CoremodHook;

public class EventRegenMana extends Event {
    public final EntityLivingBase entity;
    public final float currentMana;
    public final float initialManaChange;
    public float finalManaChange;
    
    public EventRegenMana(EntityLivingBase entity, float currentMana, float initialRegenAmount) {
        this.entity = entity;
        this.currentMana = currentMana;
        this.finalManaChange = this.initialManaChange = initialRegenAmount;
    }
    
    @CoremodHook
    public static float onRegenMana(float newMana, float currentMana, EntityLivingBase entity) {
        float initialRegenAmount = newMana - currentMana;
        EventRegenMana event = new EventRegenMana(entity, currentMana, initialRegenAmount);
        MinecraftForge.EVENT_BUS.post(event);
        // Negative values have undefined behavior, so avoid them
        float finalMana = currentMana + event.finalManaChange;
        if (finalMana < 0.0F) {
            return 0.0F;
        }
        return finalMana;
    }
}
