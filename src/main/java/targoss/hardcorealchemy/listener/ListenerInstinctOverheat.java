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

package targoss.hardcorealchemy.listener;

import net.minecraft.entity.player.EntityPlayer;
import targoss.hardcorealchemy.config.Configs;

public class ListenerInstinctOverheat extends ConfiguredListener {
    public ListenerInstinctOverheat(Configs configs) {
        super(configs);
    }
    
    public static boolean isOverheating(EntityPlayer player) {
        // TODO: Implement
        /*
        instinct = getInstinct();
        if (instinct == null) {
            return false;
        }
        dataOverheat = instinct.getEffectData(Instincts.OVERHEAT);
        if (dataOverheat.isOverheating()) {
            return false;
        }
         */
        return false;
    }
    
    // TODO: Uncomment when we find the event
    //@SubscribeEvent
    void onPunchBlock() {
        // TODO
        /*
        if (!isOverheating(player)) {
            return;
        }
        BlockPos pos = getBlockEffectivelyPlacingOn();
        world.setAflame(pos);
         */
    }
    
    // TODO: Uncomment if we find a hook, otherwise delete this function
    //@CoremodHook
    void onPutOutFire() {
        // TODO
        /*
        if (!isOverheating(player)) {
            return;
        }
        event.setCanceled(true);
         */
    }

}
