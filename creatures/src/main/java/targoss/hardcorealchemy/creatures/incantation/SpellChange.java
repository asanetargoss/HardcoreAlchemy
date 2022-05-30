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

package targoss.hardcorealchemy.creatures.incantation;

import java.util.List;
import java.util.Random;

import com.google.common.base.Predicate;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextComponentTranslation;
import targoss.hardcorealchemy.HardcoreAlchemyCore;
import targoss.hardcorealchemy.capability.humanity.ICapabilityHumanity;
import targoss.hardcorealchemy.capability.humanity.LostMorphReason;
import targoss.hardcorealchemy.capability.humanity.ProviderHumanity;
import targoss.hardcorealchemy.creatures.util.MorphState;
import targoss.hardcorealchemy.incantation.api.ISpell;
import targoss.hardcorealchemy.util.Chat;
import targoss.hardcorealchemy.util.IMorphExtension;
import targoss.hardcorealchemy.util.MorphExtension;

public class SpellChange implements ISpell {
    protected Random random = new Random();

    @Override
    public boolean canInvoke(EntityPlayerMP player) {
        return true;
    }
    
    protected static int randomMostLikelyZero(Random random, int size) {
        if (random.nextDouble() < 0.75) {
            return 0;
        }
        double value = random.nextDouble();
        int index = (int)Math.floor(value * value * value * value * value * value * size);
        return index < size ? index : 0;
    }

    @Override
    public void invoke(EntityPlayerMP player) {
        ICapabilityHumanity humanity = player.getCapability(ProviderHumanity.HUMANITY_CAPABILITY, null);
        if (humanity == null) {
            return;
        }
        if (!humanity.canMorph()) {
            Chat.message(Chat.Type.NOTIFY, player, humanity.explainWhyCantMorph());
            return;
        }

        float maxDistance = 30.0F;

        AxisAlignedBB aabb = player.getEntityBoundingBox().expandXyz(maxDistance);
        Predicate<EntityLivingBase> ableToMorphInto = new IMorphExtension.MorphablePredicate();
        List<EntityLivingBase> nearbyEntities = MorphExtension.INSTANCE.getEntitiesAndMorphsExcluding(player, player.world, EntityLivingBase.class, aabb, ableToMorphInto);
        if (nearbyEntities.size() == 0) {
            Chat.message(Chat.Type.NOTIFY, player, new TextComponentTranslation("hardcorealchemy.incantation.change.error.no_nearby_entities"));
            return;
        }
        nearbyEntities.sort(new IMorphExtension.DistanceComparator(player.posX, player.posY, player.posZ));
        int entitySelected = randomMostLikelyZero(random, nearbyEntities.size());
        EntityLivingBase entity = nearbyEntities.get(entitySelected);
        // CHAAAAANGE!
        MorphState.forceForm(HardcoreAlchemyCore.proxy.configs, player, LostMorphReason.LOST_HUMANITY, entity);
        // A thunderous boom for magical effect
        player.world.playSound(null, player.getPosition(), SoundEvents.ENTITY_LIGHTNING_THUNDER, SoundCategory.WEATHER,
                100.0F, 0.8F + (random.nextFloat() - random.nextFloat()) * 0.8F);
    }
}
