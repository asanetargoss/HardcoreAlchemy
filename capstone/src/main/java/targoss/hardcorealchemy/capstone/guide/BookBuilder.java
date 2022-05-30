/*
 * Copyright 2017-2022 asanetargoss
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

package targoss.hardcorealchemy.capstone.guide;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import amerifrance.guideapi.api.GuideAPI;
import amerifrance.guideapi.api.IPage;
import amerifrance.guideapi.api.impl.Book;
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
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

@SuppressWarnings("deprecation")
public class BookBuilder {
    
    public static class Category {
        public static class Result {
            protected String id;
            protected String itemId;
            
            protected static class Entry {
                protected String id;
                protected String itemId;
                protected String[] pageIds;
            }
            
            //NOTE: If this needs to be public, then it should be deep copied in build()
            protected List<Entry> entries = new ArrayList<>();
        }
        
        protected Result result = new Result();
        
        public Category setId(String id) {
            result.id = id;
            return this;
        }
        
        public Category setItemId(String itemId) {
            result.itemId = itemId;
            return this;
        }
        
        public Category addEntry(String id, String itemId, String...pageNames) {
            Result.Entry entry = new Result.Entry();
            entry.id = id;
            entry.itemId = itemId;
            entry.pageIds = pageNames;
            result.entries.add(entry);
            return this;
        }
        
        public Result build() {
            // Copy state
            Result result = new Result();
            result.id = this.result.id;
            result.itemId = this.result.itemId;
            result.entries.addAll(this.result.entries);
            return result;
        }
    }
    
    public static class Result {
        public Book book;
        protected String namespace;
        protected String id;
        // Ends with "."
        protected String base;
        
        protected List<Category.Result> categories = new ArrayList<>();
        
        public Book constructBook() {
            base = namespace + "." + id + ".";
            book.setAuthor(base + "author");
            book.setRegistryName(namespace, id);
            book.setDisplayName(base + "display");
            book.setTitle(base + "title");
            book.setWelcomeMessage(base + "welcome");
            return book;
        }
        
        public Book registerBookAndModel() {
            GameRegistry.register(book);
            if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
                GuideAPI.setModel(book);
            }

            return book;
        }
        
        protected void addBookCategory(Category.Result category) {
            String categoryName = base + category.id;
            // LinkedHashMap maintains iteration order
            Map<ResourceLocation, EntryAbstract> bookEntryList = new LinkedHashMap<>();
            
            for (Category.Result.Entry entry : category.entries) {
                List<IPage> pageList = new ArrayList<>();
                String entryName = categoryName + "." + entry.id;
                
                for (String pageId : entry.pageIds) {
                    String pageName = entryName + "." + pageId;
                    pageList.addAll(PageHelper.pagesForLongText(
                            I18n.translateToLocal(pageName),
                            252));
                }
                
                Item item = Item.getByNameOrId(entry.itemId);
                if (item == null) {
                    Block block = Block.getBlockFromName(entry.itemId);
                    if (block != null) {
                        item = Item.getItemFromBlock(block);
                    }
                }
                Entry bookEntry = new EntryItemStack(pageList,
                        entryName + ".title",
                        new ItemStack(item));
                
                bookEntryList.put(new ResourceLocation(entryName), bookEntry);
            }
            amerifrance.guideapi.api.impl.Category bookCategory =
                    new CategoryItemStack(bookEntryList,
                            categoryName + ".title",
                            new ItemStack(Item.getByNameOrId(category.itemId)));
            book.addCategory(bookCategory);
        }
        
        public Book registerCategories() {
            // Categories are added here rather than preInit because the registered items must exist
            for (Category.Result category : categories) {
                addBookCategory(category);
            }
            return book;
        }
    }
    
    protected Result result = new Result();
    
    public BookBuilder setNamespace(String namespace) {
        result.namespace = namespace;
        return this;
    }
    
    public BookBuilder setID(String id) {
        result.id = id;
        return this;
    }
    
    public BookBuilder addCategory(Category.Result category) {
        result.categories.add(category);
        return this;
    }
    
    public Result build() {
        // Copy state and init the book
        Result result = new Result();
        result.book = new Book();
        result.namespace = this.result.namespace;
        result.id = this.result.id;
        result.categories.addAll(this.result.categories);
        return result;
    }
}
