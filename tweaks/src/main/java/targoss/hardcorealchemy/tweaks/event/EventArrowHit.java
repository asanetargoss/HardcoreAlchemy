/*
 * Copyright 2017-2022 asanetargoss
 *
 * This file is part of Hardcore Alchemy Tweaks.
 *
 * Hardcore Alchemy Tweaks is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Tweaks is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Tweaks. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.tweaks.event;

import net.minecraft.util.DamageSource;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;
import targoss.hardcorealchemy.coremod.CoremodHook;

public class EventArrowHit extends Event {
    public final RayTraceResult rayTraceResult;
    public final DamageSource damageSource;
    
    public EventArrowHit(DamageSource damageSource, RayTraceResult rayTraceResult) {
        this.damageSource = damageSource;
        this.rayTraceResult = rayTraceResult;
    }
    
    @CoremodHook
    public static void onArrowHit(DamageSource damageSource, RayTraceResult rayTraceResult) {
        EventArrowHit event = new EventArrowHit(damageSource, rayTraceResult);
        MinecraftForge.EVENT_BUS.post(event);
    }
}
