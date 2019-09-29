/*
 * Copyright 2018 asanetargoss
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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import amerifrance.guideapi.api.IPage;
import amerifrance.guideapi.api.impl.Category;
import amerifrance.guideapi.api.impl.Entry;
import amerifrance.guideapi.api.impl.abstraction.EntryAbstract;
import amerifrance.guideapi.api.util.PageHelper;
import amerifrance.guideapi.category.CategoryItemStack;
import amerifrance.guideapi.entry.EntryItemStack;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;

public class CategoryBuilder {
    // Maintains iteration order
    private Map<ResourceLocation, EntryAbstract> entryList = new LinkedHashMap<>();
    private String categoryName;
    private Category category;
    
    private CategoryBuilder(String categoryName, String itemId) {
        this.categoryName = categoryName;
        this.category = new CategoryItemStack(this.entryList, categoryName + ".title",
                new ItemStack(Item.getByNameOrId(itemId)));
    }
    
    public static CategoryBuilder withCategory(String categoryName, String itemId) {
        CategoryBuilder builder = new CategoryBuilder(categoryName, itemId);
        return builder;
    }
    
    public CategoryBuilder addEntry(String entryName, String itemId, String... pageNames) {
        List<IPage> pageList = new ArrayList<>();
        for (String s : pageNames) {
            pageList.addAll(PageHelper.pagesForLongText(
                    I18n.translateToLocal(categoryName + "." + entryName + "." + s),
                    252));
        }
        
        Item item = Item.getByNameOrId(itemId);
        // TODO: See if this actually fixes the crash
        if (item == null) {
            Block block = Block.getBlockFromName(itemId);
            if (block != null) {
                item = Item.getItemFromBlock(block);
            }
        }
        
        Entry entry = new EntryItemStack(pageList,
                categoryName + "." + entryName + ".title",
                new ItemStack(item));
        
        this.entryList.put(new ResourceLocation(categoryName + "." + entryName), entry);
        // Just in case...
        this.category.entries = this.entryList;
        
        return this;
    }
    
    public Category getCategory() {
        return this.category;
    }
}