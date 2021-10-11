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

package targoss.hardcorealchemy.tweaks.event;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Like LivingAttackEvent, except you can modify the attack value, and cannot cancel it.
 * Useful when you want to modify the damage before armor is applied.
 */
//TODO: consider bringing a version of this event to Forge itself
public class EventLivingAttack extends Event {
    public final EntityLivingBase entity;
    public final DamageSource source;
    public float amount;
    
    public EventLivingAttack(EntityLivingBase entity, DamageSource source, float amount) {
        this.entity = entity;
        this.source = source;
        this.amount = amount;
    }
    
    public static float onLivingAttack(EntityLivingBase entity, DamageSource source, float amount) {
        EventLivingAttack event = new EventLivingAttack(entity, source, amount);
        return (MinecraftForge.EVENT_BUS.post(event) ? 0.0F : event.amount);
    }
}
