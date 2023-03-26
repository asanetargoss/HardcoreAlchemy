package targoss.hardcorealchemy.block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.base.Function;

import net.minecraft.block.Block;
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
    
    public BlockModelInfo(Block block, @Nullable Class<T> teClass) {
        this.block = block;
        this.teClass = teClass;
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
    
    protected static IBakedModel getDummyBakedModel() {
        Map<EnumFacing, List<BakedQuad>> faceQuads = new HashMap<>();
        for (EnumFacing facing : EnumFacing.values()) {
            faceQuads.put(facing, new ArrayList<>());
        }
        dummyBakedModel = new SimpleBakedModel(new ArrayList<>(), faceQuads, false, false, ModelLoader.White.INSTANCE, ItemCameraTransforms.DEFAULT, ItemOverrideList.NONE);
        return dummyBakedModel;
    }
    
    // TODO: Is the correct vertex format ITEM, or something else?
    public IBakedModel getBakedModel() {
        if (bakedModel == null) {
            try {
                net.minecraftforge.client.model.IModel model = ModelLoaderRegistry.getModel(id);
                bakedModel = model.bake(TRSRTransformation.identity(), DefaultVertexFormats.ITEM, SimpleTextureGetter.INSTANCE);
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