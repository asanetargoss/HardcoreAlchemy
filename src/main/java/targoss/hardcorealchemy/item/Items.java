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
import static targoss.hardcorealchemy.item.HcAPotion.GOOD_EFFECT;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.Color;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import targoss.hardcorealchemy.HardcoreAlchemy;

public class Items {
    private static boolean recipesRegistered = false;
    private static List<Item> ITEM_CACHE = new ArrayList<>();
    private static List<Potion> POTION_CACHE = new ArrayList<>();
    private static List<PotionType> POTION_TYPE_CACHE = new ArrayList<>();
    
    public static final Item ESSENCE_MAGE = item("essence_mage");
    
    public static final Potion POTION_ALLOW_MAGIC = potion("allow_magic", GOOD_EFFECT, new Color(113, 80, 182), 0, true);
    public static final PotionType POTION_TYPE_ALLOW_MAGIC = potionType(POTION_ALLOW_MAGIC, 5*60*20);
    //public static final Potion POTION_AIR_BREATHING = potion("air_breathing", new PotionAirBreathing(GOOD_EFFECT, new Color(86, 211, 212), 1, false));
    public static final Potion POTION_AIR_BREATHING = potion("air_breathing", new PotionAirBreathing(GOOD_EFFECT, new Color(205, 205, 205), 1, false));
    public static final PotionType POTION_TYPE_AIR_BREATHING = potionType(POTION_AIR_BREATHING, 3*60*20);
    public static final PotionType POTION_TYPE_AIR_BREATHING_EXTENDED = potionType(POTION_AIR_BREATHING, "_extended", 8*60*20);
    
    private static Item item(String itemName, Item item) {
        item.setRegistryName(HardcoreAlchemy.MOD_ID, itemName);
        // Only used by Item.toString(), but may be useful for debugging
        item.setUnlocalizedName(item.getRegistryName().toString());
        ITEM_CACHE.add(item);
        return item;
    }
    
    private static Item item(String itemName) {
        Item item = new Item();
        return item(itemName, item);
    }
    
    private static Potion potion(String potionName, Potion potion) {
        potion.setPotionName("potion." + HardcoreAlchemy.MOD_ID + ":" + potionName);
        potion.setRegistryName(HardcoreAlchemy.MOD_ID, potionName);
        POTION_CACHE.add(potion);
        return potion;
    }
    
    private static Potion potion(String potionName, boolean isBadEffect, Color color, int locationId, boolean halfPixelOffsetRight) {
        HcAPotion potion = new HcAPotion(isBadEffect, color, locationId, halfPixelOffsetRight);
        return potion(potionName, potion);
    }
    
    private static PotionType potionType(Potion potion, String registrySuffix, int duration) {
        PotionType type = new PotionType(
                potion.getRegistryName().toString(),
                new PotionEffect[]{new PotionEffect(potion, duration)});
        type.setRegistryName(potion.getRegistryName().toString() + registrySuffix);
        POTION_TYPE_CACHE.add(type);
        return type;
    }
    
    private static PotionType potionType(Potion potion, int duration) {
        return potionType(potion, "", duration);
    }
    
    public static void registerItems() {
        boolean isClient = FMLCommonHandler.instance().getSide() == Side.CLIENT;
        for (Item item : ITEM_CACHE) {
            GameRegistry.register(item);
            if (isClient) {
                registerModel(item);
            }
        }
        ITEM_CACHE.clear();
    }
    
    @SideOnly(Side.CLIENT)
    public static void registerModel(Item item) {
        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), null));
    }
    
    public static void registerPotions() {
        for (Potion potion : POTION_CACHE) {
            GameRegistry.register(potion);
        }
        POTION_CACHE.clear();
        
        for (PotionType potionType : POTION_TYPE_CACHE) {
            GameRegistry.register(potionType);
        }
        POTION_TYPE_CACHE.clear();
    }
    
    public static void registerRecipes() {
        if (recipesRegistered) {
            return;
        }
        recipesRegistered = true;
        
        ItemStack lapis = new ItemStack(Item.getByNameOrId("dye"));
        lapis.setItemDamage(EnumDyeColor.BLUE.getDyeDamage());
        GameRegistry.addShapelessRecipe(
                new ItemStack(ESSENCE_MAGE),
                new ItemStack(Item.getByNameOrId("rotten_flesh")),
                new ItemStack(Item.getByNameOrId("soul_sand")),
                new ItemStack(Item.getByNameOrId("book")),
                lapis
                );
        BrewingRecipeRegistry.addRecipe(
                PotionUtils.addPotionToItemStack(new ItemStack(Item.getByNameOrId("potion")), PotionType.getPotionTypeForName("awkward")),
                new ItemStack(ESSENCE_MAGE),
                PotionUtils.addPotionToItemStack(new ItemStack(Item.getByNameOrId("potion")), POTION_TYPE_ALLOW_MAGIC)
                );
        BrewingRecipeRegistry.addRecipe(
                PotionUtils.addPotionToItemStack(new ItemStack(Item.getByNameOrId("potion")), PotionType.getPotionTypeForName("water_breathing")),
                new ItemStack(FERMENTED_SPIDER_EYE),
                PotionUtils.addPotionToItemStack(new ItemStack(Item.getByNameOrId("potion")), POTION_TYPE_AIR_BREATHING)
                );
        BrewingRecipeRegistry.addRecipe(
                PotionUtils.addPotionToItemStack(new ItemStack(Item.getByNameOrId("potion")), PotionType.getPotionTypeForName("long_water_breathing")),
                new ItemStack(FERMENTED_SPIDER_EYE),
                PotionUtils.addPotionToItemStack(new ItemStack(Item.getByNameOrId("potion")), POTION_TYPE_AIR_BREATHING_EXTENDED)
                );
        BrewingRecipeRegistry.addRecipe(
                PotionUtils.addPotionToItemStack(new ItemStack(Item.getByNameOrId("potion")), POTION_TYPE_AIR_BREATHING),
                new ItemStack(REDSTONE),
                PotionUtils.addPotionToItemStack(new ItemStack(Item.getByNameOrId("potion")), POTION_TYPE_AIR_BREATHING_EXTENDED)
                );
    }
}
