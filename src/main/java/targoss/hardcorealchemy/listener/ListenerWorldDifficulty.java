package targoss.hardcorealchemy.listener;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.DimensionType;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import targoss.hardcorealchemy.capability.serverdata.CapabilityServerData;
import targoss.hardcorealchemy.capability.serverdata.ICapabilityServerData;
import targoss.hardcorealchemy.capability.serverdata.ProviderServerData;
import targoss.hardcorealchemy.config.Configs;

/**
 * Use a capability stored in the overworld to check if
 * the difficulty level has been set to hard yet. (We
 * only set it once because we want it to be like the
 * "default" difficulty)
 */
public class ListenerWorldDifficulty extends ConfiguredListener {

    public ListenerWorldDifficulty(Configs configs) {
        super(configs);
    }
    
    public void serverStarting(FMLServerStartingEvent event) {
        MinecraftServer server = event.getServer();
        World world = server.worldServerForDimension(DimensionType.OVERWORLD.getId());
        if (world.hasCapability(ProviderServerData.SERVER_DATA_CAPABILITY, null)) {
            ICapabilityServerData worldCap = world.getCapability(ProviderServerData.SERVER_DATA_CAPABILITY, null);
            if (!worldCap.getHasDifficulty()) {
                server.setDifficultyForAllWorlds(EnumDifficulty.HARD);
                worldCap.setHasDifficulty(true);
            }
        }
    }
    
    @SubscribeEvent
    public void onWorldCapability(AttachCapabilitiesEvent<World> event) {
        World world = event.getObject();
        MinecraftServer server = world.getMinecraftServer();
        if (server != null &&
                server.worldServerForDimension(DimensionType.OVERWORLD.getId()) == world) {
            event.addCapability(CapabilityServerData.RESOURCE_LOCATION, new ProviderServerData());
        }
    }
}
