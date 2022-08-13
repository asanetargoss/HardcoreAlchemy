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

package targoss.hardcorealchemy.creatures.item;

import static net.minecraft.init.Items.FERMENTED_SPIDER_EYE;
import static net.minecraft.init.Items.REDSTONE;
import static targoss.hardcorealchemy.item.HcAPotion.GOOD_EFFECT;
import static targoss.hardcorealchemy.item.Items.EMPTY_SLATE;
import static targoss.hardcorealchemy.item.Items.ITEMS;
import static targoss.hardcorealchemy.item.Items.POTIONS;
import static targoss.hardcorealchemy.item.Items.POTION_TYPES;
import static targoss.hardcorealchemy.item.Items.addPotionRecipe;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.IForgeRegistry;
import targoss.hardcorealchemy.item.HcAPotion;
import targoss.hardcorealchemy.item.ReusableShapelessRecipe;
import targoss.hardcorealchemy.registrar.RegistrarPotionType;
import targoss.hardcorealchemy.util.Color;

public class Items {
    public static final Item DROWNED_ENDER_PEARL = ITEMS.add("drowned_ender_pearl", new Item());
    public static final Item ASH = ITEMS.add("ash", new Item());
    public static final Item SEAL_OF_FORM = ITEMS.add("seal_of_form", new ItemSealOfForm());
    
    public static final Potion POTION_AIR_BREATHING = POTIONS.add("air_breathing", new PotionAirBreathing(GOOD_EFFECT, new Color(205, 205, 205), 1, false));
    public static final Potion POTION_WATER_RESISTANCE = POTIONS.add("water_resistance", new HcAPotion(GOOD_EFFECT, new Color(47, 107, 58), 2, false));
    
    public static final PotionType POTION_TYPE_AIR_BREATHING = POTION_TYPES.add("air_breathing", RegistrarPotionType.potionTypeFromPotion(POTION_AIR_BREATHING, 3*60*20));
    public static final PotionType POTION_TYPE_AIR_BREATHING_EXTENDED = POTION_TYPES.add("air_breathing_extended", RegistrarPotionType.potionTypeFromPotion(POTION_AIR_BREATHING, 8*60*20));
    public static final PotionType POTION_TYPE_WATER_RESISTANCE = POTION_TYPES.add("water_resistance", RegistrarPotionType.potionTypeFromPotion(POTION_WATER_RESISTANCE, 3*60*20));
    public static final PotionType POTION_TYPE_WATER_RESISTANCE_EXTENDED = POTION_TYPES.add("water_resistance_extended", RegistrarPotionType.potionTypeFromPotion(POTION_WATER_RESISTANCE, 8*60*20));

    /** Not a real recipe. Used by JEI lookup only. */
    public static final RecipeEnchantment RECIPE_ENCHANTMENT_CREATE_SEAL_OF_FORM = new RecipeEnchantment(EMPTY_SLATE, SEAL_OF_FORM);

    public static void registerRecipes() {
        {
            ItemStack lapis = new ItemStack(Item.getByNameOrId("dye"));
            lapis.setItemDamage(EnumDyeColor.BLUE.getDyeDamage());
            ItemStack flint = new ItemStack(Item.getByNameOrId("flint"));
            
            List<ItemStack> ingredients = Lists.<ItemStack>newArrayList(
                lapis,
                new ItemStack(Item.getByNameOrId("water_bucket")),
                flint,
                new ItemStack(Item.getByNameOrId("ender_pearl"))
                );
            List<ItemStack> toReuse = Lists.<ItemStack>newArrayList(flint);
            CraftingManager.getInstance().addRecipe(new ReusableShapelessRecipe(
                    new ItemStack(DROWNED_ENDER_PEARL),
                    ingredients,
                    toReuse
                ));
        }
        
        IForgeRegistry<PotionType> potionTypeRegistry = GameRegistry.findRegistry(PotionType.class);

        PotionType awkwardPotion = potionTypeRegistry.getValue(new ResourceLocation("awkward"));
        
        addPotionRecipe(
                potionTypeRegistry.getValue(new ResourceLocation("water_breathing")),
                new ItemStack(FERMENTED_SPIDER_EYE),
                POTION_TYPE_AIR_BREATHING,
                false
                );
        addPotionRecipe(
                potionTypeRegistry.getValue(new ResourceLocation("long_water_breathing")),
                new ItemStack(FERMENTED_SPIDER_EYE),
                POTION_TYPE_AIR_BREATHING_EXTENDED,
                false
                );
        addPotionRecipe(
                POTION_TYPE_AIR_BREATHING,
                new ItemStack(REDSTONE),
                POTION_TYPE_AIR_BREATHING_EXTENDED,
                false
                );
        addPotionRecipe(
                awkwardPotion,
                new ItemStack(DROWNED_ENDER_PEARL),
                POTION_TYPE_WATER_RESISTANCE,
                false
                );
        addPotionRecipe(
                POTION_TYPE_WATER_RESISTANCE,
                new ItemStack(REDSTONE),
                POTION_TYPE_WATER_RESISTANCE_EXTENDED,
                false
                );
    }
    
    public static class ClientSide {
        public static void registerModels() {
            Minecraft.getMinecraft().getItemColors().registerItemColorHandler(ItemSealOfForm.Colors.INSTANCE, SEAL_OF_FORM);
        }
    }
}
