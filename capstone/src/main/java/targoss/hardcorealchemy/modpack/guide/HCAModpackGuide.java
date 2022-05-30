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

package targoss.hardcorealchemy.modpack.guide;

import static targoss.hardcorealchemy.tweaks.item.Items.DIMENSIONAL_FLUX_CRYSTAL;

import amerifrance.guideapi.api.GuideAPI;
import amerifrance.guideapi.api.impl.Book;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.item.Items;

public class HCAModpackGuide {
    private static BookBuilder.Result guideBuildResult;
    public static Book guide = null;
    
    public static void preInit() {
        guideBuildResult = new BookBuilder().setNamespace(HardcoreAlchemy.MOD_ID).setID("guide")
                .addCategory(new BookBuilder.Category().setId("modpack_info").setItemId("sign")
                        .addEntry("about_the_pack", "compass", "1", "2", "3")
                        .addEntry("keybinding_primer", "feather", "1", "2", "3")
                        .addEntry("jei_primer", "crafting_table", numStrings(1, 6))
                        .addEntry("known_issues", "poisonous_potato", numStrings(1, 6))
                        .build())
                .addCategory(new BookBuilder.Category().setId("unexpected_journey").setItemId("adinferos:pocket_wormhole")
                        .addEntry("intro", "wooden_axe", numStrings(1, 5))
                        .addEntry("settling_in", "armor_stand", numStrings(1, 7))
                        .addEntry("humans", "emerald", numStrings(1, 3))
                        .addEntry("the_nether", "quartz_ore", numStrings(1, 6))
                        .build())
                .addCategory(new BookBuilder.Category().setId("exploring_the_arcane").setItemId("bloodmagic:ItemFluidRouterFilter")
                        .addEntry("intro", "bookshelf", "1")
                        .addEntry("alchemic_ash", "AlchemicAsh:VitalCatalyst", "1")
                        .addEntry("blood_magic", "bloodmagic:ItemBloodShard", "1")
                        .addEntry("embers", "embers:ancient_motive_core", "1", "2")
                        .addEntry("ars_magica", "arsmagica2:arcane_compendium", "1")
                        .addEntry("astral_sorcery", "astralsorcery:ItemWand", "1")
                        .addEntry("projecte", "projecte:item.pe_philosophers_stone", "1")
                        .addEntry("thaumcraft", "thaumcraft:goggles", "1")
                        .addEntry("unique_crops", "uniquecrops:seednormal", "1")
                        .build())
                .addCategory(new BookBuilder.Category().setId("morphing").setItemId(Items.ESSENCE_MAGE.getRegistryName().toString())
                        .addEntry("intro", "golden_apple", numStrings(1, 3))
                        .addEntry("abilities", "ender_pearl", "1")
                        .addEntry("lost_humanity", "rotten_flesh", numStrings(1, 3))
                        .addEntry("instinct_attack_prey", "bone", "1")
                        .addEntry("ability_primitive_sustenance", "grass", "1")
                        .addEntry("instinct_homesick_nature", "sapling", "1")
                        .addEntry("instinct_homesick_nether", "magma_cream", "1")
                        .build())
                .addCategory(new BookBuilder.Category().setId("curiosities").setItemId(DIMENSIONAL_FLUX_CRYSTAL.getRegistryName().toString())
                        .addEntry("dimensional_flux_crystals", DIMENSIONAL_FLUX_CRYSTAL.getRegistryName().toString(), "1", "2")
                        .build())
                .build();

        guide = guideBuildResult.constructBook();
        guideBuildResult.registerBookAndModel();
    }
    
    public static void init() {
        GameRegistry.addShapelessRecipe(GuideAPI.getStackFromBook(guide),
                new ItemStack(Item.getByNameOrId("dirt")),
                new ItemStack(Item.getByNameOrId("dirt")));
        guideBuildResult.registerCategories();
    }
    
    public static String[] numStrings(int start, int end) {
        int count = end - start + 1;
        String[] strings = new String[count];
        for (int i = 0; i < count; i++) {
            strings[i] = Integer.toString(start + i);
        }
        
        return strings;
    }
}
