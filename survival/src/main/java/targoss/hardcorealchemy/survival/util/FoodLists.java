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

package targoss.hardcorealchemy.survival.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import targoss.hardcorealchemy.util.InventoryUtil;
import targoss.hardcorealchemy.util.MorphDiet;

public class FoodLists {
    // For each of these, if meta is not 0 then the item name is domain:itemname:meta, otherwise the item name is domain:itemname
    
    public static Map<String, MorphDiet.Restriction> itemRestrictions = new HashMap<String, MorphDiet.Restriction>();
    public static Map<String, MorphDiet.Restriction> oreDictRestrictions = new HashMap<String, MorphDiet.Restriction>();
    
    // True lookup means the item's dietary restriction is ignored when used in a crafting recipe
    public static Map<String, Boolean> itemCraftIgnore = new HashMap<String, Boolean>();
    // True lookup means the oredict member's dietary restriction is ignored when used in a crafting recipe
    public static Map<String, Boolean> oreDictCraftIgnore = new HashMap<String, Boolean>();
    
    public static Set<String> itemFoodIngredients = new HashSet<String>();
    public static Set<String> oreFoodIngredients = new HashSet<String>();
    
    static {
        MorphDiet.Restriction vegan = MorphDiet.Restriction.VEGAN;
        MorphDiet.Restriction carnivore = MorphDiet.Restriction.CARNIVORE;
        MorphDiet.Restriction omnivore = MorphDiet.Restriction.OMNIVORE;
        Map<String, MorphDiet.Restriction> items = itemRestrictions;
        Map<String, MorphDiet.Restriction> ores = oreDictRestrictions;
        @SuppressWarnings("unused")
        Set<String> ingredients = itemFoodIngredients;
        Set<String> oreIngredients = oreFoodIngredients;
        
        // Pam's Harvestcraft oredictionaries
        
        ores.put("listAllfruit", vegan);
        ores.put("listAllveggie", vegan);
        ores.put("listAllseed", vegan);
        ores.put("listAllgrain", vegan);
        ores.put("listAllnut", vegan);
        
        ores.put("listAllmeatraw", carnivore);
        ores.put("listAllmeatcooked", carnivore);
        ores.put("listAllfishraw", carnivore);
        ores.put("listAllfishcooked", carnivore);
        ores.put("listAllegg", carnivore);
        ores.put("listAllmilk", carnivore);
        
        ores.put("listAllspice", vegan);
        oreDictCraftIgnore.put("listAllspice", true);
        ores.put("listAllherb", vegan);
        oreDictCraftIgnore.put("listAllherb", true);
        ores.put("listAllpepper", vegan);
        oreDictCraftIgnore.put("listAllpepper", true);

        oreIngredients.add("foodFlour");
        oreIngredients.add("foodDough");
        oreIngredients.add("foodPasta");
        oreIngredients.add("foodOliveoil");
        oreIngredients.add("foodSesameoil");
        oreIngredients.add("listAllsugar");
        
        // Vanilla Minecraft
        
        items.put("minecraft:apple", vegan);
        items.put("minecraft:red_mushroom", vegan);
        items.put("minecraft:brown_mushroom", vegan);
        items.put("minecraft:reeds", vegan);
        items.put("minecraft:porkchop", carnivore);
        items.put("minecraft:cooked_porkchop", carnivore);
        items.put("minecraft:milk_bucket", carnivore);
        items.put("minecraft:egg", carnivore);
        items.put("minecraft:fish", carnivore);
        items.put("minecraft:cooked_fish", carnivore);
        items.put("minecraft:melon", vegan);
        items.put("minecraft:beef", carnivore);
        items.put("minecraft:cooked_beef", carnivore);
        items.put("minecraft:chicken", carnivore);
        items.put("minecraft:cooked_chicken", carnivore);
        items.put("minecraft:rotten_flesh", carnivore);
        items.put("minecraft:bone", carnivore);
        items.put("minecraft:beef", carnivore);
        items.put("minecraft:leather", carnivore);
        items.put("minecraft:spider_eye", carnivore);
        items.put("minecraft:carrot", vegan);
        items.put("minecraft:potato", vegan);
        items.put("minecraft:baked_potato", vegan);
        items.put("minecraft:poisonous_potato", vegan);
        items.put("minecraft:rabbit", carnivore);
        items.put("minecraft:cooked_rabbit", carnivore);
        items.put("minecraft:mutton", carnivore);
        items.put("minecraft:cooked_mutton", carnivore);
        items.put("minecraft:chorus_fruit", vegan);
        items.put("minecraft:beetroot", vegan);
        items.put("minecraft:pumpkin", vegan);
        items.put("minecraft:melon_block", vegan);
        items.put("minecraft:cactus", vegan);
        items.put("minecraft:wheat", vegan);
        items.put("minecraft:bread", vegan);
        items.put("minecraft:dye:1", vegan); // Rose red
        items.put("minecraft:dye:11", vegan); // Dandelion yellow
        items.put("minecraft:dye:3", vegan); // Cocoa beans
        items.put("minecraft:dye:0", carnivore); // Ink sac
        oreIngredients.add("dye");
        
        // Pam's Harvestcraft
        
        // Harvestcraft vegan overrides for tofu-based foods
        String[] cookTypes = new String[]{"raw", "cooked"};
        
        String[] tofuTypes = new String[]{"tofish", "tofeak",
                "tofacon", "tofegg", "tofeeg"/*harvestcraft typo*/,
                "tofutton", "toficken", "tofabbit", "tofurkey",
                "tofenison"};
        for (String cookType : cookTypes) {
            for (String tofuType : tofuTypes) {
                items.put("harvestcraft:" + cookType + tofuType + "item",
                        vegan);
            }
        }
        
        // Harvestcraft seafood not in either raw/cooked fish oredict
        String[] seafoodTypes = new String[]{"clam", "crab",
                "crayfish", "frog", "octopus", "scallop",
                "shrimp", "snail", "turtle", "calamari"};
        for (String cookType : cookTypes) {
            for (String seafoodType : seafoodTypes) {
                // Notice the parameter order is different from the vegan items
                items.put("harvestcraft:" + seafoodType + cookType + "item",
                        carnivore);
            }
        }
        items.put("harvestcraft:jellyfishrawitem", carnivore);
        
        items.put("harvestcraft:firmtofuitem", vegan);
        items.put("harvestcraft:silkentofuitem", vegan);
        items.put("harvestcraft:soymilkitem", vegan);
        
        // Unique Crops
        
        items.put("uniquecrops:generic:6", vegan);
        items.put("uniquecrops:genericfood.teriyaki", omnivore);
        items.put("uniquecrops:genericfood.heart", carnivore);
        items.put("uniquecrops:genericfood.waffle", vegan);
        
        // Ad Inferos
        
        items.put("adinferos:cooked_flesh", carnivore);
        items.put("adinferos:purple_mushroom", vegan);
        items.put("adinferos:cooked_purple_mushroom", vegan);
        
        // Village Box
        
        items.put("villagebox:grape", vegan);
        items.put("villagebox:lemon", vegan);
        items.put("villagebox:orange", vegan);
        items.put("villagebox:pineapple", vegan);
        items.put("villagebox:strawberry", vegan);
        items.put("villagebox:melon", vegan);
        items.put("villagebox:banana", vegan);
        items.put("villagebox:tofu", vegan);
        items.put("villagebox:boiled_egg", carnivore);
        items.put("villagebox:shrimp", carnivore);
        items.put("villagebox:crab", carnivore);
        items.put("villagebox:pork_ramen", omnivore);
        items.put("villagebox:beef_noodle_soup", omnivore);
        items.put("villagebox:noodle_soup", vegan);
        items.put("villagebox:carrot_cake", vegan);
        items.put("villagebox:hamburger", omnivore);
        items.put("villagebox:congee", vegan);
        items.put("villagebox:boiled_fish", carnivore);
        items.put("villagebox:apple_candy", vegan);
        items.put("villagebox:creamy_corn", vegan);
        items.put("villagebox:chocolate_banana", vegan);
        items.put("villagebox:fries", vegan);
        items.put("villagebox:mapo_tofu", vegan);
        items.put("villagebox:canned_oranges", vegan);
        items.put("villagebox:strawberry_icecream", omnivore);
        items.put("villagebox:corn_chips", vegan);
        items.put("villagebox:melon_icecream", vegan);
        items.put("villagebox:roasted_lamb", vegan);
        items.put("villagebox:nigiri", carnivore);
        items.put("villagebox:udon", vegan);
        
        // Ars Magica
        
        items.put("arsmagica2:item_ore:8", carnivore); // Pig fat
    }
    
    /**
     * Given an item stack, return the strictest dietary restriction for which
     * the food can still be consumed, or null if there is no such restriction.
     * Checks for the specific item first.
     * Falls back to ore dictionary lookup if the specific item is not found.
     * 
     * DOES NOT check for NBT/Capability/crafting history!
     */
    public static MorphDiet.Restriction getRestriction(ItemStack itemStack) {
        if (InventoryUtil.isEmptyItemStack(itemStack)) {
            return null;
        }
        Item item = itemStack.getItem();
        if (item == null) {
            return null;
        }
        
        String itemName;
        if (itemStack.getItemDamage() == 0) {
            itemName = item.getRegistryName().toString();
        }
        else {
            itemName = item.getRegistryName().toString() + ":" +
                    Integer.toString(itemStack.getItemDamage());
        }
        
        if (itemRestrictions.containsKey(itemName)) {
            return itemRestrictions.get(itemName);
        }
        
        for (int oreId : OreDictionary.getOreIDs(itemStack)) {
            String ore = OreDictionary.getOreName(oreId);
            if (oreDictRestrictions.containsKey(ore)) {
                MorphDiet.Restriction restriction = oreDictRestrictions.get(ore);
                return restriction;
            }
        }
        
        return null;
    }
    
    public static boolean getIgnoresCrafting(ItemStack itemStack) {
        if (InventoryUtil.isEmptyItemStack(itemStack)) {
            return false;
        }
        Item item = itemStack.getItem();
        if (item == null) {
            return false;
        }
        
        String itemName = item.getRegistryName().toString();
        int meta = itemStack.getMetadata();
        if (meta != 0) {
            itemName += ":" + meta;
        }
        if (itemCraftIgnore.get(itemName) == (Boolean)true) {
            return true;
        }
        for (int oreId : OreDictionary.getOreIDs(itemStack)) {
            String oreName = OreDictionary.getOreName(oreId);
            if (oreDictCraftIgnore.get(oreName) == (Boolean)true) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean isFoodOrIngredient(ItemStack itemStack) {
        if (InventoryUtil.isEmptyItemStack(itemStack)) {
            return false;
        }
        Item item = itemStack.getItem();
        
        String itemName = item.getRegistryName().toString();
        int meta = itemStack.getMetadata();
        if (meta != 0) {
            itemName += ":" + meta;
        }
        if (itemRestrictions.containsKey(itemName)) {
            return true;
        }
        if (itemFoodIngredients.contains(itemName)) {
            return true;
        }
        for (int oreId : OreDictionary.getOreIDs(itemStack)) {
            String oreName = OreDictionary.getOreName(oreId);
            if (oreDictRestrictions.containsKey(oreName)) {
                return true;
            }
            if (oreFoodIngredients.contains(oreName)) {
                return true;
            }
        }
        
        return (item instanceof ItemFood);
    }
}
