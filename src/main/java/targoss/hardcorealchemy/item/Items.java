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

package targoss.hardcorealchemy.item;

import static net.minecraft.init.Items.FERMENTED_SPIDER_EYE;
import static net.minecraft.init.Items.REDSTONE;
import static targoss.hardcorealchemy.item.HcAPotion.BAD_EFFECT;
import static targoss.hardcorealchemy.item.HcAPotion.GOOD_EFFECT;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.IForgeRegistry;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.coremod.HardcoreAlchemyPreInit;
import targoss.hardcorealchemy.registrar.Registrar;
import targoss.hardcorealchemy.registrar.RegistrarItem;
import targoss.hardcorealchemy.registrar.RegistrarPotion;
import targoss.hardcorealchemy.registrar.RegistrarPotionType;
import targoss.hardcorealchemy.util.Color;

public class Items {
    private static boolean recipesRegistered = false;
    
    public static final Registrar<Item> ITEMS = new RegistrarItem("items", HardcoreAlchemy.MOD_ID, HardcoreAlchemyPreInit.LOGGER);
    public static final Registrar<Potion> POTIONS = new RegistrarPotion("potions", HardcoreAlchemy.MOD_ID, HardcoreAlchemyPreInit.LOGGER);
    public static final Registrar<PotionType> POTION_TYPES = new RegistrarPotionType("potion types", HardcoreAlchemy.MOD_ID, HardcoreAlchemyPreInit.LOGGER);
    
    public static final Item ESSENCE_MAGE = ITEMS.add("essence_mage", new Item());
    public static final Item DROWNED_ENDER_PEARL = ITEMS.add("drowned_ender_pearl", new Item());
    public static final Item ASH = ITEMS.add("ash", new Item());
    
    public static final Potion POTION_ALLOW_MAGIC = POTIONS.add("allow_magic", new HcAPotion(GOOD_EFFECT, new Color(113, 80, 182), 0, true));
    public static final Potion POTION_AIR_BREATHING = POTIONS.add("air_breathing", new PotionAirBreathing(GOOD_EFFECT, new Color(205, 205, 205), 1, false));
    public static final Potion POTION_WATER_RESISTANCE = POTIONS.add("water_resistance", new HcAPotion(GOOD_EFFECT, new Color(47, 107, 58), 2, false));
    public static final Potion POTION_VOIDFADE = POTIONS.add("voidfade", new HcAPotion(BAD_EFFECT, new Color(94, 10, 199), 3, false));

    public static final PotionType POTION_TYPE_ALLOW_MAGIC = POTION_TYPES.add("allow_magic", RegistrarPotionType.potionTypeFromPotion(POTION_ALLOW_MAGIC, 5*60*20));
    public static final PotionType POTION_TYPE_AIR_BREATHING = POTION_TYPES.add("air_breathing", RegistrarPotionType.potionTypeFromPotion(POTION_AIR_BREATHING, 3*60*20));
    public static final PotionType POTION_TYPE_AIR_BREATHING_EXTENDED = POTION_TYPES.add("air_breathing_extended", RegistrarPotionType.potionTypeFromPotion(POTION_AIR_BREATHING, 8*60*20));
    public static final PotionType POTION_TYPE_WATER_RESISTANCE = POTION_TYPES.add("water_resistance", RegistrarPotionType.potionTypeFromPotion(POTION_WATER_RESISTANCE, 3*60*20));
    public static final PotionType POTION_TYPE_WATER_RESISTANCE_EXTENDED = POTION_TYPES.add("water_resistance_extended", RegistrarPotionType.potionTypeFromPotion(POTION_WATER_RESISTANCE, 8*60*20));
    
    private static Item potionItem;
    private static ItemStack getPotionItemStack(PotionType potionType) {
        if (potionItem == null) {
            potionItem = Item.getByNameOrId("potion");
        }
        
        ItemStack itemStack = new ItemStack(potionItem);
        NBTTagCompound potionTag = new NBTTagCompound();
        potionTag.setString("Potion", potionType.getRegistryName().toString());
        itemStack.setTagCompound(potionTag);
        return itemStack;
    }
    
    /**
     * Because BrewingRecipeRegistry.addRecipe isn't NBT sensitive...
     * ;_;
     */
    private static void addPotionRecipe(PotionType inputPotion, ItemStack inputCatalystStack, PotionType outputPotion) {
        ItemStack inputPotionStack = getPotionItemStack(inputPotion);
        ItemStack outputPotionStack = getPotionItemStack(outputPotion);
        BrewingRecipeRegistry.addRecipe(new HcABrewingRecipe(inputPotionStack, inputCatalystStack, outputPotionStack));
    }
    
    public static void registerRecipes() {
        if (recipesRegistered) {
            return;
        }
        recipesRegistered = true;
        
        ItemStack lapis = new ItemStack(Item.getByNameOrId("dye"));
        lapis.setItemDamage(EnumDyeColor.BLUE.getDyeDamage());

        ItemStack flint = new ItemStack(Item.getByNameOrId("flint"));
        
        GameRegistry.addShapelessRecipe(
                new ItemStack(ESSENCE_MAGE),
                new ItemStack(Item.getByNameOrId("rotten_flesh")),
                new ItemStack(Item.getByNameOrId("soul_sand")),
                new ItemStack(Item.getByNameOrId("book")),
                lapis
                );
        
        {
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

        
        addPotionRecipe(
                potionTypeRegistry.getValue(new ResourceLocation("awkward")),
                new ItemStack(ESSENCE_MAGE),
                POTION_TYPE_ALLOW_MAGIC
                );
        addPotionRecipe(
                potionTypeRegistry.getValue(new ResourceLocation("water_breathing")),
                new ItemStack(FERMENTED_SPIDER_EYE),
                POTION_TYPE_AIR_BREATHING
                );
        addPotionRecipe(
                potionTypeRegistry.getValue(new ResourceLocation("long_water_breathing")),
                new ItemStack(FERMENTED_SPIDER_EYE),
                POTION_TYPE_AIR_BREATHING_EXTENDED
                );
        addPotionRecipe(
                POTION_TYPE_AIR_BREATHING,
                new ItemStack(REDSTONE),
                POTION_TYPE_AIR_BREATHING_EXTENDED
                );
        addPotionRecipe(
                potionTypeRegistry.getValue(new ResourceLocation("awkward")),
                new ItemStack(DROWNED_ENDER_PEARL),
                POTION_TYPE_WATER_RESISTANCE
                );
        addPotionRecipe(
                POTION_TYPE_WATER_RESISTANCE,
                new ItemStack(REDSTONE),
                POTION_TYPE_WATER_RESISTANCE_EXTENDED
                );
    }
}
