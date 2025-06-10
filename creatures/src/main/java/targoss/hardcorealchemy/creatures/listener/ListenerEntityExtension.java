/*
 * Copyright 2017-2025 asanetargoss
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

import mchorse.metamorph.api.MorphManager;
import mchorse.metamorph.api.MorphSettings;
import mchorse.metamorph.api.abilities.IAbility;
import mchorse.metamorph.api.abilities.IAction;
import mchorse.metamorph.api.morphs.AbstractMorph;
import mchorse.metamorph.capabilities.morphing.IMorphing;
import mchorse.metamorph.capabilities.morphing.Morphing;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import targoss.hardcorealchemy.listener.HardcoreAlchemyListener;
import targoss.hardcorealchemy.util.EntityExtension;
import targoss.hardcorealchemy.util.IEntityExtension;

public class ListenerEntityExtension extends HardcoreAlchemyListener {
    public static class Wrapper implements IEntityExtension {
        
        public IEntityExtension delegate;
        
        public Wrapper(IEntityExtension delegate) {
            this.delegate = delegate;
        }
        
        protected MorphSettings getMorphEntitySettings(EntityLivingBase entity) {
            if ((entity instanceof EntityPlayer)) {
                EntityPlayer player = (EntityPlayer)entity;
                IMorphing morphing = Morphing.get(player);
                if (morphing != null) {
                    AbstractMorph morph = morphing.getCurrentMorph();
                    if (morph != null) {
                        return morph.getSettings(player);
                    }
                }
            }
            return null;
        }

        @Override
        public boolean hasWaterAllergy(EntityLivingBase entity) {
            MorphSettings morphSettings = getMorphEntitySettings(entity);
            if (morphSettings != null) {
                IAbility waterAllergy = MorphManager.INSTANCE.abilities.get("water_allergy");
                if (morphSettings.abilities.contains(waterAllergy)) {
                    return true;
                }
            }
            return delegate.hasWaterAllergy(entity);
        }

        @Override
        public boolean hasEnderWaterAllergy(EntityLivingBase entity) {
            MorphSettings morphSettings = getMorphEntitySettings(entity);
            if (morphSettings != null) {
                IAbility waterAllergy = MorphManager.INSTANCE.abilities.get("water_allergy");
                IAction teleport = MorphManager.INSTANCE.actions.get("teleport");
                if (morphSettings.abilities.contains(waterAllergy) && teleport.equals(morphSettings.action)) {
                    return true;
                }
            }
            return delegate.hasEnderWaterAllergy(entity);
        }
        
    }
    
    public void preInit(FMLPreInitializationEvent event) {
        EntityExtension.INSTANCE = new Wrapper(EntityExtension.INSTANCE);
    }

}
