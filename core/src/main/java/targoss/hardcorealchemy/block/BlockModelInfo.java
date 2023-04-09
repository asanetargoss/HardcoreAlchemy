/*
 * Copyright 2017-2023 asanetargoss
 *
 * This file is part of Hardcore Alchemy Core.
 *
 * Hardcore Alchemy Core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Core is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Core. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.base.Function;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.SimpleBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import targoss.hardcorealchemy.HardcoreAlchemyCore;

public class BlockModelInfo<T extends TileEntity> {
    public final Block block;
    public final @Nullable Class<T> teClass;
    public ResourceLocation id;
    protected static IBakedModel dummyBakedModel = null;
    protected IBakedModel bakedModel = null;
    public @Nullable ResourceLocation customParticleTexture = null;
    
    public BlockModelInfo(Block block, @Nullable Class<T> teClass) {
        this.block = block;
        this.teClass = teClass;
    }
    
    public BlockModelInfo<T> setCustomParticleTexture(ResourceLocation resource) {
        this.customParticleTexture = resource;
        return this;
    }
    
    protected static class SimpleTextureAtlasSprite extends TextureAtlasSprite {
        public SimpleTextureAtlasSprite(TextureAtlasSprite vanillaSprite) {
            super(vanillaSprite.getIconName());
            this.copyFrom(vanillaSprite);
        }
        
        @Override
        public float getInterpolatedU(double u)
        {
            return (float)u / 16.0f;
        }

        @Override
        public float getUnInterpolatedU(float u)
        {
            return u * 16.0f;
        }
        
        @Override
        public float getInterpolatedV(double v)
        {
            return (float)v / -16.0f;
        }

        @Override
        public float getUnInterpolatedV(float v)
        {
            return v * -16.0f;
        }
    }
    
    protected static class SimpleTextureGetter implements Function<ResourceLocation, TextureAtlasSprite> {
        public static final SimpleTextureGetter INSTANCE = new SimpleTextureGetter();
        @Override
        public TextureAtlasSprite apply(ResourceLocation location) {
            TextureAtlasSprite vanillaSprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString());
            return new SimpleTextureAtlasSprite(vanillaSprite);
        }
    }
    
    protected static class BakedModelWithParticles implements IBakedModel {
        protected final @Nullable IBakedModel delegate;
        protected final @Nullable TextureAtlasSprite customParticleTexture;
        
        public BakedModelWithParticles(IBakedModel delegate, @Nullable ResourceLocation customParticleTexture) {
            this.delegate = delegate;
            this.customParticleTexture = customParticleTexture == null ? null : Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(customParticleTexture.toString());
        }
        
        @Override
        public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
            return delegate.getQuads(state, side, rand);
        }

        @Override
        public boolean isAmbientOcclusion() {
            return delegate.isAmbientOcclusion();
        }

        @Override
        public boolean isGui3d() {
            return delegate.isGui3d();
        }

        @Override
        public boolean isBuiltInRenderer() {
            return delegate.isBuiltInRenderer();
        }

        @Override
        public TextureAtlasSprite getParticleTexture() {
            if (customParticleTexture != null) {
                return customParticleTexture;
            }
            return delegate.getParticleTexture();
        }

        @SuppressWarnings("deprecation")
        @Override
        public ItemCameraTransforms getItemCameraTransforms() {
            return delegate.getItemCameraTransforms();
        }

        @Override
        public ItemOverrideList getOverrides() {
            return delegate.getOverrides();
        }
        
    }
    
    protected static IBakedModel getDummyBakedModel() {
        Map<EnumFacing, List<BakedQuad>> faceQuads = new HashMap<>();
        for (EnumFacing facing : EnumFacing.values()) {
            faceQuads.put(facing, new ArrayList<>());
        }
        dummyBakedModel = new SimpleBakedModel(new ArrayList<>(), faceQuads, false, false, ModelLoader.White.INSTANCE, ItemCameraTransforms.DEFAULT, ItemOverrideList.NONE);
        return dummyBakedModel;
    }
    
    protected IBakedModel createBakedModel() throws Exception {
        net.minecraftforge.client.model.IModel model = ModelLoaderRegistry.getModel(id);
        IBakedModel objModel = model.bake(TRSRTransformation.identity(), DefaultVertexFormats.ITEM, SimpleTextureGetter.INSTANCE);
        return new BakedModelWithParticles(objModel, customParticleTexture);
    }
    
    public IBakedModel getBakedModel() {
        if (bakedModel == null) {
            try {
                bakedModel = createBakedModel();
            } catch (Exception e) {
                HardcoreAlchemyCore.LOGGER.error("Failed to get baked model '" + id + "'. Substituting a dummy one.", e);
                bakedModel = getDummyBakedModel();
            }
        }
        return bakedModel;
    }
    
    public static class Client<T extends TileEntity> {
        public final BlockModelInfo<T> info;
        public final @Nullable TileEntitySpecialRenderer<? super T> tesr;
        protected static int LIST_NULL = -2;
        protected static int LIST_ERROR = -1;
        protected int displayList = LIST_NULL;
        
        public Client(BlockModelInfo<T> info, @Nullable TileEntitySpecialRenderer<? super T> tesr) {
            this.info = info;
            this.tesr = tesr;
        }
        
        public void bindTileEntitySpecialRenderer() {
            // TODO: Is this really needed?
            ClientRegistry.bindTileEntitySpecialRenderer(info.teClass, tesr);
        }
    }
}
