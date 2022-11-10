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

import mchorse.metamorph.api.EntityUtils;
import mchorse.metamorph.api.MorphAPI;
import mchorse.metamorph.api.MorphManager;
import mchorse.metamorph.api.MorphUtils;
import mchorse.metamorph.api.morphs.AbstractMorph;
import mchorse.metamorph.api.morphs.EntityMorph;
import mchorse.metamorph.capabilities.morphing.IMorphing;
import mchorse.metamorph.capabilities.morphing.Morphing;
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
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import targoss.hardcorealchemy.HardcoreAlchemyCore;
import targoss.hardcorealchemy.capability.humanity.ICapabilityHumanity;
import targoss.hardcorealchemy.capability.humanity.MorphAbilityChangeReason;
import targoss.hardcorealchemy.capability.humanity.ProviderHumanity;
import targoss.hardcorealchemy.creatures.HardcoreAlchemyCreatures;
import targoss.hardcorealchemy.creatures.util.EntityEggUtil;
import targoss.hardcorealchemy.creatures.util.MorphState;
import targoss.hardcorealchemy.util.Chat;
import targoss.hardcorealchemy.util.Color;
import targoss.hardcorealchemy.util.EntityUtil;
import targoss.hardcorealchemy.util.Serialization;
import targoss.hardcorealchemy.util.Chat.Type;

public class ItemSealOfForm extends Item {
    public static final String HUMAN = HardcoreAlchemyCore.MOD_ID + ":" + "human";
    
    public ItemSealOfForm() {
        this.addPropertyOverride(new ResourceLocation(HUMAN), new IItemPropertyGetter() {
            public float apply(ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity) {
                return hasHumanTag(stack) ? 1.0F : 0.0F;
            }
        });
    }
    
    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand) {
        IMorphing morphing = Morphing.get(player);
        if (morphing == null) {
            return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
        }
        ICapabilityHumanity humanity = player.getCapability(ProviderHumanity.HUMANITY_CAPABILITY, null);
        if (humanity != null && !humanity.shouldDisplayHumanity()) {
            if (player.world.isRemote) {
                Chat.messageSP(Chat.Type.NOTIFY, player, humanity.explainWhyCantMorph());
            }
            return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
        }
        
        boolean used = false;
        if (hasHumanTag(stack)) {
            if (humanity.getHasForgottenMorphAbility()) {
                if (!world.isRemote) {
                    Chat.messageSP(Type.NOTIFY, player, humanity.explainWhyCantMorph());
                }
            }
            else if (humanity.getHasForgottenHumanForm()) {
                if (!world.isRemote) {
                    MorphState.forceForm(HardcoreAlchemyCore.proxy.configs, player, MorphAbilityChangeReason.REMEMBERED_HUMAN_FORM, morphing.getCurrentMorph());
                }
                used = true;
            }
            else {
                if (world.isRemote) {
                    Chat.messageSP(Chat.Type.NOTIFY, player, new TextComponentTranslation("hardcorealchemy.item.seal_of_form.use.alreadyhavehuman"));
                }
            }
        }
        else {
            AbstractMorph newMorph = MorphManager.INSTANCE.morphFromNBT(stack.getTagCompound());
            if (newMorph != null) {
                if (!morphing.acquiredMorph(newMorph)) {
                    if (!world.isRemote) {
                        MorphAPI.acquire(player, newMorph);
                    }
                    used = true;
                }
                else {
                    if (world.isRemote) {
                        Chat.messageSP(Chat.Type.NOTIFY, player, new TextComponentTranslation("hardcorealchemy.item.seal_of_form.use.alreadyhavemorph"));
                    }
                }
            }
        }

        if (used) {
            if (!player.capabilities.isCreativeMode)
            {
                --stack.stackSize;
            }
            return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
        }
        else {
            return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
        }
    }
    
    public static boolean hasHumanTag(ItemStack stack) {
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null) {
            return false;
        }
        return nbt.hasKey(HUMAN) && nbt.getBoolean(HUMAN);
    }
    
    public static @Nullable EntityMorph getEntityMorph(ItemStack stack) {
        String morphID = getMorphIDFromItem(stack);
        if (morphID == null) {
            return null;
        }
        AbstractMorph morph = MorphManager.INSTANCE.morphFromNBT(stack.getTagCompound());
        if (!(morph instanceof EntityMorph)) {
            return null;
        }
        return (EntityMorph)morph;
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
    
    public static void setEntityMorphOnItem(@Nonnull ItemStack stack, String entityID, @Nullable NBTTagCompound entityData) {
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        NBTTagCompound itemNBT = stack.getTagCompound();
        itemNBT.setString(NBT_MORPH_NAME, entityID);
        if (entityData != null) {
            itemNBT.setTag(NBT_ENTITY_DATA, EntityUtils.stripEntityNBT(entityData));
        }
    }
    
    public static void setMorphOnItem(@Nonnull ItemStack stack, @Nullable AbstractMorph morph) {
        if (morph == null) {
            ItemSealOfForm.setHumanTag(stack);
        }
        else {
            NBTTagCompound morphNBT = morph.toNBT();
            stack.setTagCompound(morphNBT);
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
    public void getSubItems(Item item, CreativeTabs tab, List<ItemStack> subItems) {
        // Special case: Human
        subItems.add(getSealHuman());
        
        // For all other seals of form, get entity spawns, filtering out blacklisted morphs.
        ensureMorphBlacklistInitialized();
        for (EntityList.EntityEggInfo eggInfo : EntityEggUtil.getEggInfos().values())
        {
            if (MorphManager.isBlacklisted(eggInfo.spawnedID)) {
                continue;
            }
            ItemStack itemstack = new ItemStack(item, 1);
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

    // TODO: Dungeon loot (add blank slate to dungeon loot maybe also?)
}
