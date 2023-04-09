package targoss.hardcorealchemy.util;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleDigging;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockUtil {
    protected static Random rand = new Random();
    
    /** Creates block hit particles using a custom sprite.
     * From ParticleManager.addBlockHitEffects (1.10) */
    @SideOnly(Side.CLIENT)
    public static void addBlockHitEffects(Block block, World world, RayTraceResult target, ParticleManager manager, TextureAtlasSprite customParticleSprite) {
        EnumFacing side = target.sideHit;
        BlockPos pos = target.getBlockPos();
        IBlockState state = world.getBlockState(pos);

        if (state.getRenderType() != EnumBlockRenderType.INVISIBLE)
        {
            int i = pos.getX();
            int j = pos.getY();
            int k = pos.getZ();
            AxisAlignedBB axisalignedbb = state.getBoundingBox(world, pos);
            double d0 = (double)i + rand.nextDouble() * (axisalignedbb.maxX - axisalignedbb.minX - 0.20000000298023224D) + 0.10000000149011612D + axisalignedbb.minX;
            double d1 = (double)j + rand.nextDouble() * (axisalignedbb.maxY - axisalignedbb.minY - 0.20000000298023224D) + 0.10000000149011612D + axisalignedbb.minY;
            double d2 = (double)k + rand.nextDouble() * (axisalignedbb.maxZ - axisalignedbb.minZ - 0.20000000298023224D) + 0.10000000149011612D + axisalignedbb.minZ;

            if (side == EnumFacing.DOWN)
            {
                d1 = (double)j + axisalignedbb.minY - 0.10000000149011612D;
            }

            if (side == EnumFacing.UP)
            {
                d1 = (double)j + axisalignedbb.maxY + 0.10000000149011612D;
            }

            if (side == EnumFacing.NORTH)
            {
                d2 = (double)k + axisalignedbb.minZ - 0.10000000149011612D;
            }

            if (side == EnumFacing.SOUTH)
            {
                d2 = (double)k + axisalignedbb.maxZ + 0.10000000149011612D;
            }

            if (side == EnumFacing.WEST)
            {
                d0 = (double)i + axisalignedbb.minX - 0.10000000149011612D;
            }

            if (side == EnumFacing.EAST)
            {
                d0 = (double)i + axisalignedbb.maxX + 0.10000000149011612D;
            }

            ParticleDigging particle = (ParticleDigging) new ParticleDigging.Factory().createParticle(-1, world, d0, d1, d2, 0.0D, 0.0D, 0.0D, Block.getIdFromBlock(block));
            particle.setParticleTexture(customParticleSprite);
            manager.addEffect(particle.setBlockPos(pos).multiplyVelocity(0.2F).multipleParticleScaleBy(0.6F));
        }
    }

    /** Creates block destroy particles using a custom sprite.
     * From ParticleManager.addBlockDestroyEffects (1.10) */
    @SideOnly(Side.CLIENT)
    public static void addBlockDestroyEffects(Block block, World world, BlockPos pos, ParticleManager manager, TextureAtlasSprite customParticleSprite)
    {
        for (int j = 0; j < 4; ++j)
        {
            for (int k = 0; k < 4; ++k)
            {
                for (int l = 0; l < 4; ++l)
                {
                    double d0 = (double)pos.getX() + ((double)j + 0.5D) / 4.0D;
                    double d1 = (double)pos.getY() + ((double)k + 0.5D) / 4.0D;
                    double d2 = (double)pos.getZ() + ((double)l + 0.5D) / 4.0D;
                    ParticleDigging particle = (ParticleDigging) new ParticleDigging.Factory().createParticle(-1, world, d0 - (double)pos.getX() - 0.5D, d1 - (double)pos.getY() - 0.5D, d2 - (double)pos.getZ() - 0.5D, 0.0D, 0.0D, 0.0D, Block.getIdFromBlock(block));
                    particle.setParticleTexture(customParticleSprite);
                    manager.addEffect(particle);
                }
            }
        }
    }
}
