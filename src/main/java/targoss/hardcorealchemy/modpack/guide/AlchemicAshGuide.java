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

import static targoss.hardcorealchemy.ModState.ALCHEMIC_ASH_ID;

import amerifrance.guideapi.api.GuideAPI;
import amerifrance.guideapi.api.impl.Book;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.ModState;

public class AlchemicAshGuide {
    private static Book guide = null;
    
    public static void registerBook() {
        Book book = getBookInstance();
        GameRegistry.register(book);
        if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
            GuideAPI.setModel(book);
        }
    }
    
    public static void registerRecipe() {
        Book book = getBookInstance();
        
        GameRegistry.addShapelessRecipe(GuideAPI.getStackFromBook(book),
                new ItemStack(Item.getByNameOrId("sand")),
                new ItemStack(Item.getByNameOrId("sand")));
    }
    
    public static Book getBookInstance() {
        if (guide != null) {
            return guide;
        }
        
        Book book = new Book();
        
        String base = "alchemicash.guide.";
        
        book.setAuthor(base + "author");
        book.setRegistryName(HardcoreAlchemy.MOD_ID, "alchemicash_guide");
        book.setDisplayName(base + "display");
        book.setTitle(base + "title");
        book.setWelcomeMessage(base + "welcome");
        
        String idBase = ALCHEMIC_ASH_ID + ":";
        
        book.addCategory(CategoryBuilder.withCategory(base + "introduction", "writable_book")
                .addEntry("welcome", "sapling", "1")
                .addEntry("disclaimer", "sign", "1")
                .getCategory());
        
        book.addCategory(CategoryBuilder.withCategory(base + "catalysts", "furnace")
                .addEntry("slime", "slime_ball", "1")
                .addEntry("vital_catalyst", idBase + "VitalCatalyst", "1")
                .addEntry("alchemic_ash", idBase + "AlchemicAsh", "1")
                .addEntry("crystal_catalyst", idBase + "CrystalCatalyst", "1")
                .addEntry("advice", "sign", "1")
                .getCategory());
        
        book.addCategory(CategoryBuilder.withCategory(base + "vital_catalyst", "slime")
                .addEntry("duplication", "grass", "1")
                .addEntry("mob_eggs", "porkchop", "1")
                .addEntry("other", "bone", "1")
                .getCategory());
        
        book.addCategory(CategoryBuilder.withCategory(base + "ash", idBase + "FloweringAsh")
                .addEntry("discovery", idBase + "AshenBale", "1")
                .addEntry("cinder_bale", idBase + "CinderBale", numStrings(1, 2))
                .addEntry("blazing_bale", idBase + "BlazingBale", numStrings(1, 2))
                .addEntry("inferno_bale", idBase + "InfernoBale", numStrings(1, 2))
                .addEntry("fuel", "coal", "1")
                .addEntry("wands", idBase + "CinderWand", "1")
                .getCategory());
        
        book.addCategory(CategoryBuilder.withCategory(base + "automation", "hopper")
                .addEntry("limitations", "sign", "1")
                .addEntry("feeders", idBase + "RedSmith", "1")
                .addEntry("eaters", idBase + "BlazeSmith", "1")
                .addEntry("other", idBase + "LavaSmith", "1")
                .getCategory());
        
        book.addCategory(CategoryBuilder.withCategory(base + "crystal_catalyst", idBase + "CrystalCatalyst")
                .addEntry("skystone", idBase + "Skystone", "1")
                .addEntry("singing_stones", idBase + "StradStone", numStrings(1,2))
                .addEntry("lightning_attractor", idBase + "LightningAttractor", "1")
                .addEntry("crystal_block", idBase + "CrystalLamp", "1")
                .getCategory());
        
        guide = book;
        return book;
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
