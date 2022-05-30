package targoss.hardcorealchemy.capstone.listener;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ResearchManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.capability.inactive.IInactiveCapabilities;
import targoss.hardcorealchemy.capstone.CapstoneModState;
import targoss.hardcorealchemy.listener.HardcoreAlchemyListener;
import targoss.hardcorealchemy.util.MorphExtension;

public class ListenerPlayerMagicState extends HardcoreAlchemyListener {
    @CapabilityInject(IInactiveCapabilities.class)
    private static final Capability<IInactiveCapabilities> INACTIVE_CAPABILITIES = null;
    
    public static final String INACTIVE_STELLAR_ALIGNMENT = HardcoreAlchemy.MOD_ID + ":stellar_alignment";
    
    /**
     * EventPriority.HIGH so it runs before ListenerPlayerMagicState.onPlayerRespawn.
     * That way, ListenerPlayerMagicState.onPlayerRespawn can clear inactive capabilities
     * afterward.
     */
    @SubscribeEvent(priority=EventPriority.HIGH)
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        EntityPlayer player = event.player;
        
        IInactiveCapabilities inactives = player.getCapability(INACTIVE_CAPABILITIES, null);
        if (inactives == null) {
            return;
        }
        
        if (CapstoneModState.isAstralSorceryLoaded) {
            // Undo any hackery from deactivating stellar alignment
            activateStellarAlignment(player);
            // Then properly clear the stellar alignment only
            clearStellarAlignment(player);
            // Then sync alignment to client manually here.
            // We can do this because this event only fires on the server.
            // We don't want to do this elsewhere because
            // it could lead to nasty nasty desyncs (theoretically).
            syncStellarAlignment(player);
        }
    }
    
    @SubscribeEvent
    public void onChangeMagicState(PlayerTickEvent event) {
        if (event.phase != Phase.END) {
            return;
        }
        
        EntityPlayer player = event.player;
        
        IInactiveCapabilities inactives = player.getCapability(INACTIVE_CAPABILITIES, null);
        if (inactives == null) {
            return;
        }
        
        if (MorphExtension.INSTANCE.canUseHighMagic(player)) {
            if (CapstoneModState.isAstralSorceryLoaded) {
                activateStellarAlignment(player);
            }
        }
        else {
            if (CapstoneModState.isAstralSorceryLoaded) {
                deactivateStellarAlignment(player);
            }
        }
    }
    
    @Optional.Method(modid=CapstoneModState.ASTRAL_SORCERY_ID)
    public static void activateStellarAlignment(EntityPlayer player) {
        PlayerProgress playerProgress = ResearchManager.getProgress(player, player.world.isRemote ? Side.CLIENT : Side.SERVER);
        if (playerProgress == null) {
            return;
        }
        
        IInactiveCapabilities inactives = player.getCapability(INACTIVE_CAPABILITIES, null);
        if (inactives == null) {
            return;
        }
        
        ConcurrentMap<String, IInactiveCapabilities.Cap> caps = inactives.getCapabilityMap();
        IInactiveCapabilities.Cap cap = caps.get(INACTIVE_STELLAR_ALIGNMENT);
        if (cap != null) {
            caps.remove(INACTIVE_STELLAR_ALIGNMENT);
            // This completely overrides the player's previous progress. In principle, should be okay...
            playerProgress.load(cap.data);
            ResearchManager.savePlayerKnowledge(player);
        }
    }
    
    @Optional.Method(modid=CapstoneModState.ASTRAL_SORCERY_ID)
    public static void deactivateStellarAlignment(EntityPlayer player) {
        PlayerProgress playerProgress = ResearchManager.getProgress(player, player.world.isRemote ? Side.CLIENT : Side.SERVER);
        if (playerProgress == null) {
            return;
        }
        
        IInactiveCapabilities inactives = player.getCapability(INACTIVE_CAPABILITIES, null);
        if (inactives == null) {
            return;
        }
        
        ConcurrentMap<String, IInactiveCapabilities.Cap> caps = inactives.getCapabilityMap();
        IInactiveCapabilities.Cap cap = caps.get(INACTIVE_STELLAR_ALIGNMENT);
        if (cap == null) {
            cap = new IInactiveCapabilities.Cap();
            cap.data = new NBTTagCompound();
            playerProgress.store(cap.data);
            caps.put(INACTIVE_STELLAR_ALIGNMENT, cap);
            
            try {
                clearStellarAlignmentProgress(playerProgress);
                // Prevent the player from discovering constellations
                List<String> knownConstellations = playerProgress.getKnownConstellations();
                knownConstellations.clear();
                for (IConstellation constellation : ConstellationRegistry.getAllConstellations()) {
                    knownConstellations.add(constellation.getUnlocalizedName());
                }
                ResearchManager.savePlayerKnowledge(player);
            }
            catch (Exception e) {
                HardcoreAlchemy.LOGGER.error("Stellar alignment could not be deactivated for player ID " + player.getUniqueID(), e);
                playerProgress.load(cap.data);
            }
        }
    }

    @Optional.Method(modid=CapstoneModState.ASTRAL_SORCERY_ID)
    public static void clearStellarAlignment(EntityPlayer player) {
        PlayerProgress playerProgress = ResearchManager.getProgress(player, player.world.isRemote ? Side.CLIENT : Side.SERVER);
        if (playerProgress == null) {
            return;
        }
        try {
            clearStellarAlignmentProgress(playerProgress);
        }
        catch (Exception e) {
            HardcoreAlchemy.LOGGER.error("Stellar alignment could not be cleared for player ID " + player.getUniqueID(), e);
        }
    }

    @Optional.Method(modid=CapstoneModState.ASTRAL_SORCERY_ID)
    public static void clearStellarAlignmentProgress(PlayerProgress playerProgress) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException {
        playerProgress.clearPerks();
        Method forceChargeMethod = PlayerProgress.class.getDeclaredMethod("forceCharge", int.class);
        forceChargeMethod.setAccessible(true);
        forceChargeMethod.invoke(playerProgress, 0);
        Field attunedConstellationField = PlayerProgress.class.getDeclaredField("attunedConstellation");
        attunedConstellationField.setAccessible(true);
        attunedConstellationField.set(playerProgress, null);
        Field wasOnceAttunedField = PlayerProgress.class.getDeclaredField("wasOnceAttuned");
        wasOnceAttunedField.setAccessible(true);
        wasOnceAttunedField.set(playerProgress, false);
    }

    @Optional.Method(modid=CapstoneModState.ASTRAL_SORCERY_ID)
    public static void syncStellarAlignment(EntityPlayer player) {
        assert(!player.world.isRemote);
        try {
            Method syncMethod = ResearchManager.class.getDeclaredMethod("pushProgressToClientUnsafe", EntityPlayer.class);
            syncMethod.setAccessible(true);
            syncMethod.invoke(null, player);
        }
        catch (Exception e) {
            HardcoreAlchemy.LOGGER.error("Stellar alignment could not be synced to client for player ID " + player.getUniqueID(), e);
        }
    }
}
