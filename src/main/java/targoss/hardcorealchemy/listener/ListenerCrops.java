package targoss.hardcorealchemy.listener;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent.CropGrowEvent;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import toughasnails.api.TANBlocks;
import toughasnails.api.config.GameplayOption;
import toughasnails.api.config.SyncedConfig;
import toughasnails.api.season.Season;
import toughasnails.api.season.SeasonHelper;
import toughasnails.api.temperature.Temperature;
import toughasnails.api.temperature.TemperatureHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import targoss.hardcorealchemy.ModState;
import targoss.hardcorealchemy.config.Configs;

/**
 * Quick and dirty; subject to change
 */
public class ListenerCrops extends ConfiguredListener {
    public ListenerCrops(Configs configs) {
        super(configs);
    }

    private static Set<String> hibernators;
    
    static {
        hibernators = new HashSet<>();
        hibernators.add("com.pam.harvestcraft.blocks.growables.BlockPamFruit");
        hibernators.add("com.pam.harvestcraft.blocks.growables.BlockPamFruitLog");
    }
    
    @Optional.Method(modid=ModState.TAN_ID)
    private static boolean isWinterAt(World world, BlockPos pos) {
        Season season = SeasonHelper.getSeasonData(world).getSubSeason().getSeason();
        if (season != Season.WINTER ||
                TemperatureHelper.isPosClimatisedForTemp(world, pos, new Temperature(1)) ||
                !SyncedConfig.getBooleanValue(GameplayOption.ENABLE_SEASONS)
                ) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Coremod hook. See TCrops
     */
    public static void checkDecay(IBlockState blockState, World world, BlockPos pos) {
        if (ModState.isTanLoaded && isWinterAt(world, pos)) {
            world.setBlockState(pos, TANBlocks.dead_crops.getDefaultState());
        }
    }
    
    /**
     * Coremod hook. See TCrops
     */
    public static boolean shouldHibernate(IBlockState blockState, World world, BlockPos pos) {
        if (!ModState.isTanLoaded ||
                !isWinterAt(world, pos)) {
            return false;
        }
        return true;
    }
}
