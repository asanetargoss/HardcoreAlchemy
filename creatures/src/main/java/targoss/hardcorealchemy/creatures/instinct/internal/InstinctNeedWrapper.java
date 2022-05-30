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

package targoss.hardcorealchemy.creatures.instinct.internal;

import mchorse.metamorph.api.morphs.AbstractMorph;
import mchorse.metamorph.api.morphs.EntityMorph;
import mchorse.metamorph.capabilities.morphing.IMorphing;
import mchorse.metamorph.capabilities.morphing.Morphing;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.creatures.instinct.api.IInstinctNeed;
import targoss.hardcorealchemy.creatures.instinct.api.InstinctNeedFactory;
import targoss.hardcorealchemy.creatures.instinct.api.InvalidNeed;
import targoss.hardcorealchemy.creatures.instinct.network.api.INeedMessenger;

public class InstinctNeedWrapper {
    /** Backpointer to the factory. Needed for proper serialization. */
    public InstinctNeedFactory factory;
    /** May be null; consider using getNeed. */
    public IInstinctNeed need;
    /** Almost always null temporary variable. Needed after deserialization. */
    public NBTTagCompound needData;
    /** State information that the instinct need will have access to during events */
    public InstinctState state = new InstinctState();
    /** When the need grows, a message can be displayed by the instinct need.
     * This indicates the most severe status since a message was displayed.
     * It is reset when the instinct bar becomes full.
     * Server-side only.
     */
    public InstinctState.NeedStatus mostSevereStatusSinceMessage = InstinctState.NeedStatus.NONE;
    /** When a message is displayed on NeedStatus change, player.ticksExisted is
     * recorded here. The value is then referenced to delay when the message is
     * displayed again. 
     * Server-side only.
     */
    public int playerTickSinceInstinctFull = 0;
    
    public IInstinctNeed getNeed(EntityPlayer player) {
        if (need == null) {
            if (factory == null) {
                throw new IllegalStateException("An instance of " + getClass().getSimpleName() + " has no defined instinct factory");
            }
            
            IMorphing morphing = Morphing.get(player);
            EntityLivingBase morphEntity = null;
            if (morphing != null) {
                AbstractMorph morph = morphing.getCurrentMorph();
                if (morph != null && (morph instanceof EntityMorph)) {
                    morphEntity = ((EntityMorph)morph).getEntity(player.world);
                }
            }
            if (morphEntity == null) {
                throw new IllegalStateException("Cannot define an instinct need unless the player is an entity morph");
            }
            
            need = factory.createNeed(morphEntity);
            if (need == null) {
                HardcoreAlchemy.LOGGER.error("Failed to create instinct need for factory: '" +
                        factory.getRegistryName().toString() +
                        "', when morphed as: '" +
                        EntityList.getEntityString(morphEntity) +
                        "'. The instinct and morph may be incompatible.");
                need = InvalidNeed.INSTANCE;
            }
            
            INeedMessenger messenger = need.getCustomMessenger();
            if (messenger != null) {
                state.messenger = messenger;
            }
            
            if (needData != null) {
                need.deserializeNBT(needData);
                needData = null;
            }
        }
        
        return need;
    }
    
    public InstinctState getState(EntityPlayer player) {
        state.player = player;
        return state;
    }
}
