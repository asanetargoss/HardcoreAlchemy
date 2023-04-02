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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader.White;
import targoss.hardcorealchemy.capability.VirtualCapabilityManager;
import targoss.hardcorealchemy.tweaks.capability.itemcontainer.ICapabilityItemContainer;
import targoss.hardcorealchemy.tweaks.capability.itemcontainer.ProviderItemContainer;
import targoss.hardcorealchemy.util.InventoryUtil;

public class ModelTimefrozen implements IBakedModel {
    protected static class FrozenPropertyGetter implements IItemPropertyGetter {
        protected final float value;
        
        public FrozenPropertyGetter(float value) {
            this.value = value;
        }

        @Override
        public float apply(ItemStack stack, World worldIn, EntityLivingBase entityIn) {
            return value;
        }
    }
    
    /**
     * Some sneaky state handling to overcome the fact that an
     * IBakedModel normally doesn't know what item is about to be rendered.
     * How to get the item about to be rendered? We will have to guess...
     * by using the ItemStack, model, etc provided when
     * currentOverrides.applyOverride(...) is called
     */
    public Overrides currentOverrides = new Overrides(this);
    public IBakedModel delegate;
    
    public static class Overrides extends ItemOverrideList {
        /** It is not uncommon for this statement to evaluate to true:
         * (currentModel.currentOverrides == this).
         * Should be OK as the JVM GC can detect circular references */
        public ModelTimefrozen currentModel;
        public IBakedModel modelDelegate;
        public ItemOverrideList delegate;
        /** This item stores the model properties of pretty much every
         * much every timefrozen item that has ever been rendered this
         * session, but that should be okay because:
         * 1. The number of types of timefrozen items is usually small,
         * 2. This is a singleton,
         * 3. If two timefrozen items use the same property getter key,
         *    this will store the property value of the current ItemStack
         *    being rendered.
         * If we really wanted to, we could replace this Item and ItemStack
         * from time to time, if point 1 does not hold. 
         * */
        protected Item frozenPropertiesItem = new Item();
        protected ItemStack frozenPropertiesItemStack = new ItemStack(frozenPropertiesItem);

        public Overrides(ModelTimefrozen currentModel) {
            super(new ArrayList<>());
            this.currentModel = currentModel;
        }

        protected static final ItemStack APPLE = new ItemStack(Items.APPLE);
        
        public void resetModel() {
            this.modelDelegate = null;
            currentModel.delegate = null;
            this.delegate = null;
        }
        
        public void initializeModel(ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity) {
            if (modelDelegate == null) {
                ICapabilityItemContainer itemContainer = VirtualCapabilityManager.INSTANCE.getVirtualCapability(stack, ProviderItemContainer.CAPABILITY_ITEM_CONTAINER, false);
                if (itemContainer != null) {
                    ItemStack containedItem = itemContainer.getContainedItem();
                    if (!InventoryUtil.isEmptyItemStack(containedItem)) {
                        modelDelegate = Minecraft.getMinecraft().ingameGUI.itemRenderer.getItemModelMesher().getItemModel(containedItem);
                    }
                }
                if (modelDelegate == null) {
                    // Fallback
                    modelDelegate = Minecraft.getMinecraft().ingameGUI.itemRenderer.getItemModelWithOverrides(APPLE, world, entity);
                }
            }
        }
        
        public void propagateDelegates() {
            currentModel.currentOverrides = this;
            currentModel.delegate = this.modelDelegate;
            if (delegate == null && modelDelegate != null) {
                delegate = modelDelegate.getOverrides();
            }
        }
        
        protected ItemStack getStackWithFrozenProperties(ItemStack stack, Map<ResourceLocation, Float> properties) {
            for (Map.Entry<ResourceLocation, Float> entry : properties.entrySet()) {
                ResourceLocation key = entry.getKey();
                if (stack.getItem().getPropertyGetter(key) != null) {
                    Float value = properties.get(key);
                    if (value != null) {
                        frozenPropertiesItem.addPropertyOverride(key, new FrozenPropertyGetter(value));
                    }
                }
            }
            return frozenPropertiesItemStack;
        }
        
        @Nullable
        @Deprecated
        public ResourceLocation applyOverride(ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity) {
            initializeModel(stack, world, entity);
            propagateDelegates();
            if (this.delegate != null) {
                return this.delegate.applyOverride(stack, world, entity);
            }
            return super.applyOverride(stack, world, entity);
        }
        
        public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity) {
            initializeModel(stack, world, entity);
            propagateDelegates();
            if (this.delegate != null) {
                ICapabilityItemContainer itemContainer = VirtualCapabilityManager.INSTANCE.getVirtualCapability(stack, ProviderItemContainer.CAPABILITY_ITEM_CONTAINER, false);
                if (modelDelegate != null && itemContainer != null && !InventoryUtil.isEmptyItemStack(itemContainer.getContainedItem())) {
                    ItemStack stackWithFrozenProperties = getStackWithFrozenProperties(itemContainer.getContainedItem(), itemContainer.getPropertyOverrides());
                    return this.delegate.handleItemState(modelDelegate, stackWithFrozenProperties, world, entity);
                }
                else {
                    return this.delegate.handleItemState(originalModel, stack, world, entity);
                }
            }
            return super.handleItemState(originalModel, stack, world, entity);
        }
        
        public com.google.common.collect.ImmutableList<ItemOverride> getOverrides() {
            propagateDelegates();
            // this.delegate should be non-null at this point
            if (this.delegate != null) {
                return this.delegate.getOverrides();
            }
            return super.getOverrides();
        }
    }

    @Override
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        if (this.delegate != null) {
            return this.delegate.getQuads(state, side, rand);
        }
        return new ArrayList<BakedQuad>();
    }

    @Override
    public boolean isAmbientOcclusion() {
        if (this.delegate != null) {
            return this.delegate.isAmbientOcclusion();
        }
        return false;
    }

    @Override
    public boolean isGui3d() {
        if (this.delegate != null) {
            return this.delegate.isGui3d();
        }
        return false;
    }

    @Override
    public boolean isBuiltInRenderer() {
        if (this.delegate != null) {
            return this.delegate.isBuiltInRenderer();
        }
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        if (this.delegate != null) {
            return this.delegate.getParticleTexture();
        }
        return White.INSTANCE;
    }

    @SuppressWarnings("deprecation")
    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        if (this.delegate != null) {
            return this.delegate.getItemCameraTransforms();
        }
        return ItemCameraTransforms.DEFAULT;
    }

    @Override
    public ItemOverrideList getOverrides() {
        currentOverrides.resetModel();
        return currentOverrides;
    }

}
