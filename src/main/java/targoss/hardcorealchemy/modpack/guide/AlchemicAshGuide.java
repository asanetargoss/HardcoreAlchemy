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

import amerifrance.guideapi.api.GuideAPI;
import amerifrance.guideapi.api.impl.Book;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class AlchemicAshGuide {
    private static BookBuilder.Result guideBuildResult;
    public static Book guide = null;
    
    public static void preInit() {
        String namespace = "alchemicash";
        guideBuildResult = new BookBuilder().setNamespace(namespace).setID("guide")
                .addCategory(new BookBuilder.Category().setId("introduction").setItemId("writable_book")
                        .addEntry("welcome", "sapling", "1")
                        .addEntry("disclaimer", "sign", "1")
                        .build())
                .addCategory(new BookBuilder.Category().setId("catalysts").setItemId("furnace")
                        .addEntry("slime", "slime_ball", "1")
                        .addEntry("vital_catalyst", namespace + ":VitalCatalyst", "1")
                        .addEntry("alchemic_ash", namespace + ":AlchemicAsh", "1")
                        .addEntry("crystal_catalyst", namespace + ":CrystalCatalyst", "1")
                        .addEntry("advice", "sign", "1")
                        .build())
                .addCategory(new BookBuilder.Category().setId("vital_catalyst").setItemId("slime")
                        .addEntry("duplication", "grass", "1")
                        .addEntry("mob_eggs", "porkchop", "1")
                        .addEntry("other", "bone", "1")
                        .build())
                .addCategory(new BookBuilder.Category().setId("ash").setItemId(namespace + ":FloweringAsh")
                        .addEntry("discovery", namespace + ":AshenBale", "1")
                        .addEntry("cinder_bale", namespace + ":CinderBale", numStrings(1, 3))
                        .addEntry("blazing_bale", namespace + ":BlazingBale", numStrings(1, 2))
                        .addEntry("inferno_bale", namespace + ":InfernoBale", numStrings(1, 3))
                        .addEntry("fuel", "coal", "1")
                        .build())
                .addCategory(new BookBuilder.Category().setId("automation").setItemId("hopper")
                        .addEntry("limitations", "sign", "1")
                        .addEntry("feeders", namespace + ":RedSmith", "1")
                        .addEntry("eaters", namespace + ":BlazeSmith", "1")
                        .addEntry("other", namespace + ":LavaSmith", "1")
                        .build())
                .addCategory(new BookBuilder.Category().setId("crystal_catalyst").setItemId(namespace + ":CrystalCatalyst")
                        .addEntry("skystone", namespace + ":Skystone", "1")
                        .addEntry("singing_stones", namespace + ":StradStone", numStrings(1,2))
                        .addEntry("lightning_attractor", namespace + ":LightningAttractor", "1")
                        .addEntry("crystal_block", namespace + ":CrystalLamp", "1")
                        .build())
                .build();
        
        guide = guideBuildResult.constructBook();
        guideBuildResult.registerBookAndModel();
    }
    
    public static void init() {
        GameRegistry.addShapelessRecipe(GuideAPI.getStackFromBook(guide),
                new ItemStack(Item.getByNameOrId("sand")),
                new ItemStack(Item.getByNameOrId("sand")));
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
