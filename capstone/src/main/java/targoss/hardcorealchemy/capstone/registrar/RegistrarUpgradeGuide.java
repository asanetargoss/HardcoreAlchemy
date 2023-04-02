/*
 * Copyright 2017-2023 asanetargoss
 *
 * This file is part of Hardcore Alchemy Capstone.
 *
 * Hardcore Alchemy Capstone is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * Hardcore Alchemy Capstone is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Hardcore Alchemy Capstone.  If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.capstone.registrar;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.logging.log4j.Logger;

import amerifrance.guideapi.api.GuideAPI;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import targoss.hardcorealchemy.capstone.HardcoreAlchemyCapstone;
import targoss.hardcorealchemy.capstone.guide.HCAModpackGuide;
import targoss.hardcorealchemy.capstone.guide.UpgradeGuide;
import targoss.hardcorealchemy.registrar.Registrar;

public class RegistrarUpgradeGuide extends Registrar<UpgradeGuide> {
    public static final int BOOK_AND_MODEL = 0;
    public static final int RECIPES = 1;
    public static final int CATEGORIES = 2;
    public static final int CLEANUP = 3;
    
    protected List<UpgradeGuide> sortedVersions = new ArrayList<UpgradeGuide>();
    protected boolean sorted = false;

    public RegistrarUpgradeGuide(String name, String namespace, Logger logger) {
        super(name, namespace, logger);
    }
    
    public <V extends UpgradeGuide> V add(String upgradeTo, V guide) {
        sorted = false;
        guide = super.add(upgradeTo, guide);
        guide.upgradeTo = upgradeTo;
        guide.bookBuilder.setNamespace(namespace);
        guide.bookBuilder.setID("upgradeguide." + upgradeTo);
        guide.bookBuilderResult = guide.bookBuilder.build();
        guide.book = guide.bookBuilderResult.constructBook();
        sortedVersions.add(guide);
        return guide;
    }
    
    protected void ensureGuidesSorted() {
        if (sorted) {
            return;
        }
        sortedVersions.sort(new Comparator<UpgradeGuide>() {
            @Override
            public int compare(UpgradeGuide o1, UpgradeGuide o2) {
                return UpgradeGuide.compareVersions(o1.upgradeTo, o2.upgradeTo);
            }
        });
        sorted = true;
    }
    
    public List<ItemStack> getUpgradeGuidesSinceVersion(String lastVersion) {
        ensureGuidesSorted();
        
        List<ItemStack> upgradeGuides = new ArrayList<>(0);
        for (int i = sortedVersions.size() - 1; i >= 0; --i) {
            UpgradeGuide upgradeGuide = sortedVersions.get(i);
            if (UpgradeGuide.compareVersions(lastVersion, upgradeGuide.upgradeTo) != -1) {
                // Already got this upgrade guide
                break;
            }
            if (UpgradeGuide.compareVersions(HardcoreAlchemyCapstone.VERSION, upgradeGuide.upgradeTo) == -1) {
                // Don't give players upgrade guides from the future
                continue;
            }
            upgradeGuides.add(GuideAPI.getStackFromBook(upgradeGuide.book));
        }
        return upgradeGuides;
    }
    
    public String getDefaultExpectedPlayerVersion() {
        ensureGuidesSorted();
        
        int index = sortedVersions.size() - 2;
        if (index < 0) {
            return "";
        } else {
            return sortedVersions.get(index).upgradeTo;
        }
    }
    
    protected Item[] getRecipeBits() {
        return new Item[]{
                null,
                Item.getByNameOrId("dirt"),
                Item.getByNameOrId("sand"),
                Item.getByNameOrId("cobblestone"),
                Item.getByNameOrId("stone"),
                Item.getByNameOrId("log"),
                Item.getByNameOrId("planks"),
                Item.getByNameOrId("stick"),
            };
    }
    
    protected static void addRecipeToUpgradeGuide(Item[] recipeBits, int i, ItemStack base, ItemStack guide) {
        if (i == 0) {
            GameRegistry.addShapedRecipe(guide, "B",
                    'B', base);
            return;
        }
        int bit0 = i & 7;
        if (bit0 == i) {
            GameRegistry.addShapedRecipe(guide, "B0",
                    'B', base,
                    recipeBits[bit0] != null ? '0' : '!', recipeBits[bit0]);
            return;
        }
        int bit1 = (i >> 3) & 7;
        int bit2 = (i >> 6) & 7;
        GameRegistry.addShapedRecipe(guide, "B0", "21",
                'B', base,
                recipeBits[bit0] != null ? '0' : '!', recipeBits[bit0],
                recipeBits[bit1] != null ? '1' : '!', recipeBits[bit1],
                recipeBits[bit2] != null ? '2' : '!', recipeBits[bit2]);
    }

    public boolean register(int phase) {
        if (!super.register(phase)) {
            return false;
        }

        switch(phase) {
        case BOOK_AND_MODEL:
            for (UpgradeGuide guide : entries) {
                guide.bookBuilderResult.registerBookAndModel();
            }
            return true;
        case RECIPES:
            // Use to generate unique recipes up to recipeBits.length ^ # slots
            // recipeBits.length = 8, # slots = 3 => 512 unique recipes
            // Recipe components are arranged in the numerical order below:
            //   B 0
            //   2 1
            // Where B is the base item, The Traveling Alchemist
            Item[] recipeBits = getRecipeBits();
            assert(recipeBits.length == 8);
            for (int i = 0; i < entries.size(); ++i) {
                ItemStack base = GuideAPI.getStackFromBook(HCAModpackGuide.guide);
                UpgradeGuide entry = entries.get(i);
                ItemStack guide = GuideAPI.getStackFromBook(entry.book);
                addRecipeToUpgradeGuide(recipeBits, i, base, guide);
            }
            return true;
        case CATEGORIES:
            for (UpgradeGuide guide : entries) {
                guide.bookBuilderResult.registerCategories();
            }
            return true;
        case CLEANUP:
            for (UpgradeGuide guide : entries) {
                guide.bookBuilder = null;
                guide.bookBuilderResult = null;
            }
        default:
            return false;
        }
    }
}
