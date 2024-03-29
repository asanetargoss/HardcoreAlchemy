/*
 * Copyright 2017-2023 asanetargoss
 *
 * This file is part of Hardcore Alchemy Tweaks.
 *
 * Hardcore Alchemy Tweaks is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Tweaks is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Tweaks. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.tweaks.item;

import static net.minecraft.init.Blocks.MAGMA;
import static net.minecraft.init.Items.BONE;
import static net.minecraft.init.Items.DIAMOND;
import static net.minecraft.init.Items.FEATHER;
import static net.minecraft.init.Items.FIRE_CHARGE;
import static net.minecraft.init.Items.IRON_INGOT;
import static net.minecraft.init.Items.REDSTONE;
import static net.minecraft.init.Items.ROTTEN_FLESH;
import static net.minecraft.init.Items.SNOWBALL;
import static targoss.hardcorealchemy.ClientProxy.TILESET;
import static targoss.hardcorealchemy.block.Blocks.ORE_DIMENSIONAL_FLUX_CRYSTAL;
import static targoss.hardcorealchemy.item.HcAPotion.BAD_EFFECT;
import static targoss.hardcorealchemy.item.Items.HEARTS;
import static targoss.hardcorealchemy.item.Items.ITEMS;
import static targoss.hardcorealchemy.item.Items.POTIONS;
import static targoss.hardcorealchemy.item.Items.POTION_TYPES;
import static targoss.hardcorealchemy.item.Items.addPotionRecipe;

import java.util.List;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.IForgeRegistry;
import net.minecraftforge.oredict.OreDictionary;
import targoss.hardcorealchemy.HardcoreAlchemyCore;
import targoss.hardcorealchemy.heart.Heart;
import targoss.hardcorealchemy.item.HcAPotion;
import targoss.hardcorealchemy.registrar.RegistrarHeart;
import targoss.hardcorealchemy.registrar.RegistrarPotionType;
import targoss.hardcorealchemy.tweaks.capability.dimensionhistory.ICapabilityDimensionHistory;
import targoss.hardcorealchemy.tweaks.listener.ListenerEntityVoidfade;
import targoss.hardcorealchemy.util.Color;
import targoss.hardcorealchemy.util.InventoryUtil;

public class Items {
    public static final Item DIMENSIONAL_FLUX_CRYSTAL = ITEMS.add("dimensional_flux_crystal", new Item());
    public static final Item TIMEFROZEN = ITEMS.add("timefrozen", new TimefrozenItem());
    
    public static final Potion POTION_VOIDFADE = POTIONS.add("voidfade", new HcAPotion(BAD_EFFECT, new Color(94, 10, 199), 3, false));
    public static final Potion POTION_SLIP = POTIONS.add("slip", new HcAPotion(BAD_EFFECT, new Color(223, 69, 0), 4, false));
    
    public static final PotionType POTION_TYPE_VOIDFADE = POTION_TYPES.add("voidfade", RegistrarPotionType.potionTypeFromPotion(POTION_VOIDFADE, 90*20));
    public static final PotionType POTION_TYPE_VOIDFADE_EXTENDED = POTION_TYPES.add("voidfade_extended", RegistrarPotionType.potionTypeFromPotion(POTION_VOIDFADE, 6*60*20));
    
    public static final Heart HEART_FLAME = HEARTS.add("flame", new Heart(TILESET, 0, 54, 0, 63));
    public static final Heart HEART_TEARS = HEARTS.add("tears", new Heart(TILESET, 9, 54, 9, 63));
    public static final Heart HEART_HUNTER = HEARTS.add("hunter", new Heart(TILESET, 18, 54, 18, 63));
    
    public static class HeartRegistryImpl extends RegistrarHeart.RegistryBase {
        @Override
        public boolean register(List<Heart> entries) {
            assert(ITEMS.getPhase() == -1);
            for (Heart entry : entries) {
                entry.ITEM = new ItemHeart(entry);
                entry.ITEM_SHARD = new ItemHeartShard(entry);
                ITEMS.add("heart_" + entry.name, entry.ITEM);
                ITEMS.add("heart_" + entry.name + "_shard", entry.ITEM_SHARD);
            }
            return true;
        }
    }
    
    static {
        if (HEARTS instanceof RegistrarHeart) {
            ((RegistrarHeart)HEARTS).IMPL = new HeartRegistryImpl();
        }
    }
    
    public static void registerRecipes() {
        OreDictionary.registerOre(ORE_DIMENSIONAL_FLUX_CRYSTAL, DIMENSIONAL_FLUX_CRYSTAL);
        
        GameRegistry.addRecipe(new RecipeTimefrozen(
                    new ItemStack[] {
                            InventoryUtil.ITEM_STACK_EMPTY, new ItemStack(SNOWBALL), InventoryUtil.ITEM_STACK_EMPTY,
                            new ItemStack(SNOWBALL), RecipeTimefrozen.ITEM_TO_FREEZE_WILDCARD, new ItemStack(SNOWBALL),
                            InventoryUtil.ITEM_STACK_EMPTY, new ItemStack(DIMENSIONAL_FLUX_CRYSTAL), InventoryUtil.ITEM_STACK_EMPTY
                    }, 3
                ));
        GameRegistry.addRecipe(new RecipeRemoveTimefrozen());
        
        GameRegistry.addShapedRecipe(new ItemStack(HEART_FLAME.ITEM),
            "FMF",
            "MHM",
            "FMF",
            'H', new ItemStack(HEART_FLAME.ITEM_SHARD),
            'F', new ItemStack(FIRE_CHARGE),
            'M', new ItemStack(MAGMA)
        );
        GameRegistry.addShapedRecipe(new ItemStack(HEART_TEARS.ITEM),
            "RDR",
            "FHF",
            "RFR",
            'H', new ItemStack(HEART_TEARS.ITEM_SHARD),
            'R', new ItemStack(ROTTEN_FLESH),
            'D', new ItemStack(DIAMOND),
            'F', new ItemStack(FEATHER)
        );
        GameRegistry.addShapedRecipe(new ItemStack(HEART_HUNTER.ITEM),
            "III",
            "IHI",
            "BBB",
            'H', new ItemStack(HEART_HUNTER.ITEM_SHARD),
            'I', new ItemStack(IRON_INGOT),
            'B', new ItemStack(BONE)
        );
        
        IForgeRegistry<PotionType> potionTypeRegistry = GameRegistry.findRegistry(PotionType.class);
        PotionType awkwardPotion = potionTypeRegistry.getValue(new ResourceLocation("awkward"));
        
        addPotionRecipe(
                awkwardPotion,
                new ItemStack(DIMENSIONAL_FLUX_CRYSTAL),
                POTION_TYPE_VOIDFADE,
                false
                );
        addPotionRecipe(
                POTION_TYPE_VOIDFADE,
                new ItemStack(REDSTONE),
                POTION_TYPE_VOIDFADE_EXTENDED,
                false
                );
    }
    
    public static class ClientSide {
        public static final ResourceLocation PROPERTY_NATIVE_DIMENSION = new ResourceLocation(HardcoreAlchemyCore.MOD_ID, "native_dimension");
        
        public static void registerSpecialModels() {
            DIMENSIONAL_FLUX_CRYSTAL.addPropertyOverride(PROPERTY_NATIVE_DIMENSION, new IItemPropertyGetter() {
                @Override
                public float apply(ItemStack itemStack, World world, EntityLivingBase entity) {
                    int dimension;
                    if (world == null || world.provider == null) {
                        dimension = -1; // Nether
                    } else {
                        dimension = world.provider.getDimension();
                    }
                    ICapabilityDimensionHistory history = ListenerEntityVoidfade.getOrInitDimensionHistoryInPlace(itemStack, dimension);
                    if (history == null) {
                        return dimension;
                    }
                    return (float)history.getDimensionHistory().get(0);
                }
            });
        }
        
        public static void onModelBake(ModelBakeEvent event) {
            // ModelLoader.setCustomModelResourceLocation is already called, so we just need to override the default model somehow
            event.getModelRegistry().putObject(new ModelResourceLocation(TIMEFROZEN.getRegistryName(), null), new ModelTimefrozen());
        }
    }
}
