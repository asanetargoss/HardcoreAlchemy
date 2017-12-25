package targoss.hardcorealchemy.listener;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.oredict.OreDictionary;
import targoss.hardcorealchemy.ModState;
import targoss.hardcorealchemy.config.Configs;
import targoss.hardcorealchemy.coremod.CoremodHook;
import toughasnails.api.TANBlocks;
import toughasnails.api.config.GameplayOption;
import toughasnails.api.config.SyncedConfig;
import toughasnails.api.season.Season;
import toughasnails.api.season.SeasonHelper;
import toughasnails.api.temperature.Temperature;
import toughasnails.api.temperature.TemperatureHelper;

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
     * Coremod hook. See TDecayingCrops
     */
    @CoremodHook
    public static void checkDecay(IBlockState blockState, World world, BlockPos pos) {
        if (ModState.isTanLoaded && isWinterAt(world, pos)) {
            world.setBlockState(pos, TANBlocks.dead_crops.getDefaultState());
        }
    }
    
    /**
     * Coremod hook. See THibernatingCrops
     */
    @CoremodHook
    public static boolean shouldHibernate(IBlockState blockState, World world, BlockPos pos) {
        if (!ModState.isTanLoaded ||
                !isWinterAt(world, pos)) {
            return false;
        }
        return true;
    }
    
    /**
     * Coremod hook. See TRightClickHarvesting
     * Returns true unless this is a player holding a stick in their main hand
     */
    @CoremodHook
    public static boolean allowRightClickHarvest(RightClickBlock event) {
        EntityPlayer player = event.getEntityPlayer();
        if (player == null) {
            return true;
        }
        
        if (isStick(player.getHeldItemMainhand()) ||
                isStick(player.getHeldItemOffhand())) {
            return false;
        }
        
        return true;
    }
    
    public static boolean isStick(ItemStack stack) {
        if (stack == null) {
            return false;
        }
        
        // Is the player holding a stick? (Check the ore dictionary)
        int[] ores = OreDictionary.getOreIDs(stack);
        int stick = OreDictionary.getOreID("stickWood");
        for (int ore : ores) {
            if (ore == stick) {
                return true;
            }
        }
        
        return false;
    }
}
