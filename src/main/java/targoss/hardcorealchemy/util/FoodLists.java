package targoss.hardcorealchemy.util;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import targoss.hardcorealchemy.HardcoreAlchemy;

public class FoodLists {
    public static Map<String, MorphDiet.Restriction> itemRestrictions = new HashMap<String, MorphDiet.Restriction>();
    public static Map<String, MorphDiet.Restriction> oreDictRestrictions = new HashMap<String, MorphDiet.Restriction>();
    
    // True lookup means the item's dietary restriction is ignored when used in a crafting recipe
    public static Map<String, Boolean> itemCraftIgnore = new HashMap<String, Boolean>();
    // True lookup means the oredict member's dietary restriction is ignored when used in a crafting recipe
    public static Map<String, Boolean> oreDictCraftIgnore = new HashMap<String, Boolean>();
    
    static {
        MorphDiet.Restriction vegan = MorphDiet.Restriction.VEGAN;
        MorphDiet.Restriction carnivore = MorphDiet.Restriction.CARNIVORE;
        Map<String, MorphDiet.Restriction> items = itemRestrictions;
        Map<String, MorphDiet.Restriction> ores = oreDictRestrictions;
        
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
        
        // Vanilla Minecraft
        items.put("minecraft:apple", vegan);
        items.put("minecraft:red_mushroom", vegan);
        items.put("minecraft:brown_mushroom", vegan);
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
        items.put("minecraft:beef", carnivore);
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
        items.put("minecraft:wheat", vegan);
        items.put("minecraft:bread", vegan);
        
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
        if (itemStack == null) {
            return null;
        }
        Item item = itemStack.getItem();
        if (item == null) {
            return null;
        }
        
        String itemName = item.getRegistryName().toString();
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
        if (itemStack == null) {
            return false;
        }
        Item item = itemStack.getItem();
        if (item == null) {
            return false;
        }
        
        String itemName = item.getRegistryName().toString();
        if (itemCraftIgnore.get(itemName) == (Boolean)true) {
            return true;
        }
        for (int oreId : OreDictionary.getOreIDs(itemStack)) {
            if (oreDictCraftIgnore.get(oreId) == (Boolean)true) {
                return true;
            }
        }
        return false;
    }
}
