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

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import mchorse.metamorph.api.morphs.AbstractMorph;
import mchorse.metamorph.capabilities.morphing.IMorphing;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityEndermite;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.entity.monster.EntityPolarBear;
import net.minecraft.entity.monster.EntityShulker;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import targoss.hardcorealchemy.capability.humanity.ICapabilityHumanity;
import targoss.hardcorealchemy.listener.HardcoreAlchemyListener;
import targoss.hardcorealchemy.util.INutritionExtension;
import targoss.hardcorealchemy.util.MobLists;
import targoss.hardcorealchemy.util.MorphDiet;
import targoss.hardcorealchemy.util.MorphDiet.Needs;
import targoss.hardcorealchemy.util.MorphDiet.Restriction;
import targoss.hardcorealchemy.util.MorphExtension;
import targoss.hardcorealchemy.util.NutritionExtension;

public class ListenerNutritionExtension extends HardcoreAlchemyListener {
    public static class Wrapper implements INutritionExtension {
        @CapabilityInject(IMorphing.class)
        public static final Capability<IMorphing> MORPHING_CAPABILITY = null;
        @CapabilityInject(ICapabilityHumanity.class)
        public static final Capability<ICapabilityHumanity> HUMANITY_CAPABILITY = null;
        
        public INutritionExtension delegate;

        public Wrapper(INutritionExtension delegate) {
            this.delegate = delegate;
        }
        
        @Override
        public @Nonnull Needs getNeeds(EntityPlayer player) {
            if (MorphExtension.INSTANCE.isGhost(player)) {
                return NO_NEEDS;
            }
            ICapabilityHumanity humanityCapability = player.getCapability(HUMANITY_CAPABILITY, null);
            if (humanityCapability == null) {
                return delegate.getNeeds(player);
            }
            if (humanityCapability.canMorph()) {
                return delegate.getNeeds(player);
            }
            IMorphing morphing = player.getCapability(MORPHING_CAPABILITY, null);
            AbstractMorph morph = morphing.getCurrentMorph();
            if (morph == null) {
                return delegate.getNeeds(player);
            }
            Needs needs = morphDiets.get(morph.name);
            if (needs == null) {
                return delegate.getNeeds(player);
            }
            return needs;
        }

        @Override
        public void restoreNutrient(EntityPlayer player, String nutrientName, float amount) {
            delegate.restoreNutrient(player, nutrientName, amount);
        }

        @Override
        public void addGrassToFoodHistory(EntityPlayer player) {
            delegate.addGrassToFoodHistory(player);
        }

        @Override
        public void restoreThirst(EntityPlayer player, Needs needs, int thirstSustain, int thirstSaturationSustain) {
            delegate.restoreThirst(player, needs, thirstSustain, thirstSaturationSustain);
        }

        @Override
        public int drinkWater(EntityPlayer player, Needs needs, BlockPos pos, IBlockState blockState, int thirstSustain,
                int thirstSaturationSustain) {
            return delegate.drinkWater(player, needs, pos, blockState, thirstSustain, thirstSaturationSustain);
        }

        @Override
        public boolean isItemDrinkable(Item item) {
            return delegate.isItemDrinkable(item);
        }
        
    }
    
    /**
     * Dietary needs indexed by the fully-qualified name of the entity class which the morph represents
     */
    public static Map<String, Needs> morphDiets = new HashMap<String, Needs>();
    public static final Needs NO_NEEDS = new Needs(Needs.NO_NUTRIENTS, Restriction.UNFEEDING, false);
    public static final Needs IDEALIST_VEGAN_NEEDS = new Needs(Needs.DEFAULT_NUTRIENTS, Restriction.VEGAN);
    public static final Needs GRAZER_NEEDS = new Needs(new String[]{"grain"}, Restriction.VEGAN);
    public static final Needs NIGHT_MOB_NEEDS = new Needs(Needs.CARNIVORE_NUTRIENTS, Restriction.CARNIVORE);
    public static final Needs NETHER_MOB_NEEDS = new Needs(Needs.CARNIVORE_NUTRIENTS, Restriction.UNFEEDING, false);
    public static final Needs AURA_MOB_NEEDS = new Needs(Needs.NO_NUTRIENTS, Restriction.UNFEEDING, false);
    public static final Needs TAINT_MOB_NEEDS = new Needs(Needs.NO_NUTRIENTS, Restriction.UNFEEDING, false);
    public static final Needs ELDRITCH_MOB_NEEDS = new Needs(Needs.NO_NUTRIENTS, Restriction.UNFEEDING, false);
    
    static {
        /* With some exceptions, hostile mobs and tameables are carnivores,
         * and Nether mobs are also thirstless.
         * Passive mobs are vegans and eat wheat.
         * Exceptions/uncategorized mobs override the lists
         */
        
        for (String nightMob : MobLists.getNightMobs()) {
            morphDiets.put(nightMob, NIGHT_MOB_NEEDS);
        }
        for (String tameable : MobLists.getEntityTameables()) {
            morphDiets.put(tameable, NIGHT_MOB_NEEDS);
        }
        for (String netherMob : MobLists.getNetherMobs()) {
            morphDiets.put(netherMob, NETHER_MOB_NEEDS);
        }
        for (String passiveMob : MobLists.getPassiveMobs()) {
            morphDiets.put(passiveMob, GRAZER_NEEDS);
        }
        for (String auraMob : MobLists.getAuraMobs()) {
            morphDiets.put(auraMob, AURA_MOB_NEEDS);
        }
        for (String taintMob : MobLists.getTaintMobs()) {
            morphDiets.put(taintMob, AURA_MOB_NEEDS);
        }
        for (String eldritchMob : MobLists.getEldritchMobs()) {
            morphDiets.put(eldritchMob, ELDRITCH_MOB_NEEDS);
        }
        
        morphDiets.put(entityName(EntityCreeper.class), IDEALIST_VEGAN_NEEDS);
        morphDiets.put(entityName(EntitySlime.class), MorphDiet.PLAYER_NEEDS);
        morphDiets.put(entityName(EntityChicken.class), IDEALIST_VEGAN_NEEDS);
        morphDiets.put(entityName(EntityPig.class), IDEALIST_VEGAN_NEEDS);
        morphDiets.put("toroquest.toro", GRAZER_NEEDS);
        morphDiets.put("EnderZoo.Owl", new Needs(Needs.CARNIVORE_NUTRIENTS, Restriction.CARNIVORE, true));
        morphDiets.put(entityName(EntitySkeleton.class), new Needs(Needs.NO_NUTRIENTS, Restriction.UNFEEDING, false));
        morphDiets.put(entityName(EntityEnderman.class), new Needs(Needs.DEFAULT_NUTRIENTS, Restriction.OMNIVORE, false));
        morphDiets.put(entityName(EntityEndermite.class), new Needs(Needs.NO_NUTRIENTS, Restriction.OMNIVORE, false));
        morphDiets.put(entityName(EntitySquid.class), new Needs(Needs.CARNIVORE_NUTRIENTS, Restriction.OMNIVORE, false));
        morphDiets.put(entityName(EntityGuardian.class), new Needs(Needs.CARNIVORE_NUTRIENTS, Restriction.OMNIVORE, false));
        morphDiets.put(entityName(EntityShulker.class), new Needs(Needs.NO_NUTRIENTS, Restriction.UNFEEDING, false));
        morphDiets.put(entityName(EntityPolarBear.class), new Needs(Needs.CARNIVORE_NUTRIENTS, Restriction.OMNIVORE));
    }
    
    public static String entityName(Class<? extends Entity> clazz) {
        return EntityList.CLASS_TO_NAME.get(clazz);
    }
    
    public static String entityName(String className) {
        try {
            return EntityList.CLASS_TO_NAME.get(Class.forName(className));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void preInit(FMLPreInitializationEvent event) {
        NutritionExtension.INSTANCE = new Wrapper(NutritionExtension.INSTANCE);
    }
}
