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

package targoss.hardcorealchemy.creatures.instinct.api;

import net.minecraft.entity.player.EntityPlayer;
import targoss.hardcorealchemy.creatures.instinct.network.api.INeedMessenger;
import targoss.hardcorealchemy.creatures.instinct.network.api.NeedMessengerFullSync;

/**
 * A class sharing data between InstinctNeed and the instinct system
 */
public interface IInstinctState {
    /**
     * The player associated with this instinct
     */
    EntityPlayer getPlayer();
    
    public enum NeedStatus {
        NONE,
        WAITING,
        EVENTUALLY,
        URGENT;
    }
    
    /**
     * Tells the instinct system if this need is fulfilled or not.
     * The instinct system then may increase/decrease the instinct value accordingly.
     */
    void setNeedStatus(NeedStatus needStatus);
    
    /**
     * Sets the amplifier of an InstinctEffect that is activated or will
     * be activated. Higher values override lower values.
     * The amplifier is reset to its default when the InstinctEffect is disabled.
     * The default amplifier value of an InstinctEffect is
     * determined by the owning Instinct (via Instinct.getEffects)
     */
    void setEffectAmplifier(InstinctEffect instinctEffect, float amplifier);

    public static final INeedMessenger DEFAULT_MESSENGER = new NeedMessengerFullSync();
    
    /**
     * Gets the current object used to send instinct data over the network.
     * Interact with this object instead of sending a packet directly.
     * Defaults to IInstinctState.DEFAULT_MESSENGER.
     * A custom INeedMessenger can be set by overriding IInstinctNeed.getCustomMessenger
     */
    INeedMessenger getNeedMessenger();
}
