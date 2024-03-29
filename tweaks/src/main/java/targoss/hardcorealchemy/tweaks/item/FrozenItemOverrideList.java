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

import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class FrozenItemOverrideList extends ItemOverrideList {
    protected ItemOverrideList delegate;
    protected Item frozenPropertiesItem = new Item();
    protected ItemStack frozenPropertiesItemStack = new ItemStack(frozenPropertiesItem);

    public FrozenItemOverrideList(ItemOverrideList delegate) {
        super(Lists.<ItemOverride>newArrayList());
        this.delegate = delegate;
    }
    
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
    
    // TODO: Use hasPropertyGetter, setProperty (or maybe just setProperties below)
    public boolean hasPropertyGetter(ItemStack testStack, ResourceLocation key) {
        return testStack.getItem().getPropertyGetter(key) != null;
    }
    
    public void setProperty(ResourceLocation key, float value) {
        frozenPropertiesItem.addPropertyOverride(key, new FrozenPropertyGetter(value));
    }
    
    public void setProperties(ItemStack testStack, Map<ResourceLocation, Float> properties) {
        for (Map.Entry<ResourceLocation, Float> entry : properties.entrySet()) {
            if (hasPropertyGetter(testStack, entry.getKey())) {
                setProperty(entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    @Nullable
    @Deprecated
    public ResourceLocation applyOverride(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn)
    {
        return delegate.applyOverride(frozenPropertiesItemStack, worldIn, entityIn);
    }

    @Override
    public com.google.common.collect.ImmutableList<ItemOverride> getOverrides()
    {
        return delegate.getOverrides();
    }
}
