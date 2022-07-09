/*
 * Copyright 2017-2022 asanetargoss
 *
 * This file is part of Hardcore Alchemy Creatures.
 *
 * Hardcore Alchemy Creatures is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Creatures is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Creatures. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.creatures.item;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import mchorse.metamorph.api.MorphManager;
import mchorse.metamorph.api.MorphUtils;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityList.EntityEggInfo;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import targoss.hardcorealchemy.HardcoreAlchemyCore;
import targoss.hardcorealchemy.creatures.HardcoreAlchemyCreatures;
import targoss.hardcorealchemy.creatures.util.EntityEggUtil;
import targoss.hardcorealchemy.util.Color;
import targoss.hardcorealchemy.util.EntityUtil;
import targoss.hardcorealchemy.util.Serialization;

public class ItemSealOfForm extends Item {
    public static final String HUMAN = HardcoreAlchemyCore.MOD_ID + ":" + "human";
    
    public ItemSealOfForm() {
        this.addPropertyOverride(new ResourceLocation(HUMAN), new IItemPropertyGetter() {
            public float apply(ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity) {
                return hasHumanTag(stack) ? 1.0F : 0.0F;
            }
        });
    }
    
    public static boolean hasHumanTag(ItemStack stack) {
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null) {
            return false;
        }
        return nbt.hasKey(HUMAN) && nbt.getBoolean(HUMAN);
    }
    
    public static void setHumanTag(ItemStack stack) {
        stack.setTagInfo(HUMAN, Serialization.getBooleanTag(true));
    }

    // Based on the Metamorph entity serialization format, for future expansion.
    protected static final String NBT_MORPH_NAME = "Name";
    protected static final String NBT_ENTITY_DATA = "EntityData";
    
    protected static @Nullable String getMorphIDFromItem(@Nonnull ItemStack stack) {
        NBTTagCompound itemNBT = stack.getTagCompound();
        if (itemNBT == null) {
            return null;
        }
        if (!itemNBT.hasKey(NBT_MORPH_NAME, Serialization.NBT_STRING_ID)) {
            return null;
        }
        return itemNBT.getString(NBT_MORPH_NAME);
    }
    
    protected static void setEntityMorphOnItem(@Nonnull ItemStack stack, String entityID, @Nullable NBTTagCompound entityData) {
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        NBTTagCompound itemNBT = stack.getTagCompound();
        itemNBT.setString(NBT_MORPH_NAME, entityID);
        if (entityData != null) {
            itemNBT.setTag(NBT_ENTITY_DATA, entityData);
        }
    }
    
    public static class Colors implements IItemColor {
        public static final Colors INSTANCE = new Colors();
        
        @Override
        public int getColorFromItemstack(ItemStack stack, int tintIndex) {
            String morphID = getMorphIDFromItem(stack);
            if (morphID == null) {
                // This might be the human seal, or an invalid seal. Either way, don't change the coloring.
                return Color.WHITE_RGB;
            }
            EntityEggInfo egg = EntityEggUtil.getEggInfo(morphID);
            if (egg == null) {
                // Can happen if the entity no longer exists, or if this
                // is not a morph entity (which currently isn't implemented)
                return Color.WHITE_RGB;
            }
            if (tintIndex == 2) {
                return egg.primaryColor;
            }
            else if (tintIndex == 0) {
                return egg.secondaryColor;
            }
            else {
                return Color.WHITE_RGB;
            }
        }
    }
    
    protected static boolean blacklistInitialized = false;
    
    protected static void ensureMorphBlacklistInitialized() {
        if (!blacklistInitialized && MorphManager.INSTANCE.activeBlacklist.isEmpty()) {
            HardcoreAlchemyCreatures.LOGGER.warn("Forcing early load of the morph blacklist");
            MorphManager.INSTANCE.setActiveBlacklist(null, MorphUtils.reloadBlacklist());
        }
        blacklistInitialized = true;
    }
    
    protected static ItemStack SEAL_OF_FORM_HUMAN = null;
    protected static ItemStack getSealHuman() {
        if (SEAL_OF_FORM_HUMAN == null) {
            SEAL_OF_FORM_HUMAN = new ItemStack(Items.SEAL_OF_FORM);
            setHumanTag(SEAL_OF_FORM_HUMAN);
        }
        return SEAL_OF_FORM_HUMAN;
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
        // Special case: Human
        subItems.add(getSealHuman());
        
        // For all other seals of form, get entity spawns, filtering out blacklisted morphs.
        ensureMorphBlacklistInitialized();
        for (EntityList.EntityEggInfo eggInfo : EntityEggUtil.getEggInfos().values())
        {
            if (MorphManager.isBlacklisted(eggInfo.spawnedID)) {
                continue;
            }
            ItemStack itemstack = new ItemStack(itemIn, 1);
            setEntityMorphOnItem(itemstack, eggInfo.spawnedID, null);
            subItems.add(itemstack);
        }
    }
    
    protected static Class<? extends Entity> getCreatureClass(ItemStack stack) {
        if (hasHumanTag(stack)) {
            return EntityPlayer.class;
        }
        String morphID = getMorphIDFromItem(stack);
        if (morphID != null) {
            Class<? extends Entity> entityClass = EntityList.NAME_TO_CLASS.get(morphID);
            if (entityClass != null) {
                return entityClass;
            }
        }
        return Entity.class;
    }
    
    protected static ITextComponent UNMORPHED = new TextComponentTranslation("item.hardcorealchemy:seal_of_form.name.unmorphed");
    protected static ITextComponent getMorphName(ItemStack stack) {
        Class<? extends Entity> creatureClass = getCreatureClass(stack);
        if (creatureClass.equals(EntityPlayer.class)) {
            return UNMORPHED;
        }
        return EntityUtil.getEntityName(creatureClass);
    }
    
    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        ITextComponent morphName = getMorphName(stack);
        if (morphName == UNMORPHED) {
            return morphName.getFormattedText();
        }
        String unlocalizedName = getUnlocalizedName()  + ".name";
        ITextComponent stackName = new TextComponentTranslation(unlocalizedName, morphName);
        return stackName.getFormattedText();
    }

    // TODO: Implement JEI recipe lookup for empty slate
    // TODO: Implement JEI recipe lookup for seal of form
    // TODO: Implement morph acquire use, with NBT capability (maybe morph the player. Also, don't consume the token if the player already has the morph and display an error message instead.)
    // TODO: Dungeon loot
}
