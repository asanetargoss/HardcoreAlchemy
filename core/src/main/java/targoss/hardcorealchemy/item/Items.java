/*
 * Copyright 2017-2023 asanetargoss
 *
 * This file is part of Hardcore Alchemy Core.
 *
 * Hardcore Alchemy Core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Core is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Core. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.item;

import static targoss.hardcorealchemy.item.HcAPotion.GOOD_EFFECT;

import javax.annotation.Nullable;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.IForgeRegistry;
import net.minecraftforge.fml.common.registry.RegistryBuilder;
import targoss.hardcorealchemy.HardcoreAlchemyCore;
import targoss.hardcorealchemy.coremod.HardcoreAlchemyCoreCoremod;
import targoss.hardcorealchemy.coremod.HardcoreAlchemyPreInit;
import targoss.hardcorealchemy.heart.Heart;
import targoss.hardcorealchemy.registrar.Registrar;
import targoss.hardcorealchemy.registrar.RegistrarHeart;
import targoss.hardcorealchemy.registrar.RegistrarItem;
import targoss.hardcorealchemy.registrar.RegistrarPotion;
import targoss.hardcorealchemy.registrar.RegistrarPotionType;
import targoss.hardcorealchemy.util.Color;

public class Items {
    public static final Registrar<Item> ITEMS = new RegistrarItem("items", HardcoreAlchemyCore.MOD_ID, HardcoreAlchemyPreInit.LOGGER);
    public static final Registrar<Potion> POTIONS = new RegistrarPotion("potions", HardcoreAlchemyCore.MOD_ID, HardcoreAlchemyPreInit.LOGGER);
    public static final Registrar<PotionType> POTION_TYPES = new RegistrarPotionType("potion types", HardcoreAlchemyCore.MOD_ID, HardcoreAlchemyPreInit.LOGGER);
    /** This is in core so other mods can add hearts, however Hardcore Alchemy Tweaks must be installed for the hearts to be available in-game */
    public static final Registrar<Heart> HEARTS = new RegistrarHeart("hearts", HardcoreAlchemyCore.MOD_ID, HardcoreAlchemyPreInit.LOGGER);
    
    public static final IForgeRegistry<Heart> HEART_REGISTRY = new RegistryBuilder<Heart>()
            .setName(new ResourceLocation(HardcoreAlchemyCore.MOD_ID, "hearts"))
            .setType(Heart.class)
            .setIDRange(0, 1024)
            .create();
    
    public static final Item ESSENCE_MAGE = ITEMS.add("essence_mage", new Item());
    public static final Item EMPTY_SLATE = ITEMS.add("empty_slate", new ItemEmptySlate());
    public static final @Nullable Item LOOT_TESTER = !HardcoreAlchemyCoreCoremod.obfuscated ? ITEMS.add("loot_tester", new ItemLootTester()) : null;
    
    public static final Potion POTION_ALLOW_MAGIC = POTIONS.add("allow_magic", new HcAPotion(GOOD_EFFECT, new Color(113, 80, 182), 5, false));

    public static final PotionType POTION_TYPE_ALLOW_MAGIC = POTION_TYPES.add("allow_magic", RegistrarPotionType.potionTypeFromPotion(POTION_ALLOW_MAGIC, 5*60*20));
    
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
    public static void addPotionRecipe(PotionType inputPotion, ItemStack inputCatalystStack, PotionType outputPotion, boolean strict) {
        ItemStack inputPotionStack = getPotionItemStack(inputPotion);
        ItemStack outputPotionStack = getPotionItemStack(outputPotion);
        BrewingRecipeRegistry.addRecipe(new HcABrewingRecipe(inputPotionStack, inputCatalystStack, outputPotionStack, strict));
    }
    
    public static void registerRecipes() {
        ItemStack lapis = new ItemStack(Item.getByNameOrId("dye"));
        lapis.setItemDamage(EnumDyeColor.BLUE.getDyeDamage());
        
        GameRegistry.addShapelessRecipe(
                new ItemStack(ESSENCE_MAGE),
                new ItemStack(Item.getByNameOrId("rotten_flesh")),
                new ItemStack(Item.getByNameOrId("soul_sand")),
                new ItemStack(Item.getByNameOrId("book")),
                lapis
                );
        
        IForgeRegistry<PotionType> potionTypeRegistry = GameRegistry.findRegistry(PotionType.class);

        PotionType awkwardPotion = potionTypeRegistry.getValue(new ResourceLocation("awkward"));
        
        addPotionRecipe(
                awkwardPotion,
                new ItemStack(ESSENCE_MAGE),
                POTION_TYPE_ALLOW_MAGIC,
                false
                );
    }
}
