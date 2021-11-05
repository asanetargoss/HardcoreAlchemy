package targoss.hardcorealchemy.creatures.listener;

import mchorse.metamorph.Metamorph;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.capability.CapUtil;
import targoss.hardcorealchemy.capability.humanity.ICapabilityHumanity;
import targoss.hardcorealchemy.capability.killcount.ICapabilityKillCount;
import targoss.hardcorealchemy.capability.morphstate.ICapabilityMorphState;
import targoss.hardcorealchemy.creatures.HardcoreAlchemyCreatures;
import targoss.hardcorealchemy.creatures.capability.instinct.CapabilityInstinct;
import targoss.hardcorealchemy.creatures.capability.instinct.ICapabilityInstinct;
import targoss.hardcorealchemy.creatures.capability.instinct.ProviderInstinct;
import targoss.hardcorealchemy.creatures.capability.instinct.StorageInstinct;
import targoss.hardcorealchemy.creatures.network.MessageInstinct;
import targoss.hardcorealchemy.listener.HardcoreAlchemyListener;
import targoss.hardcorealchemy.network.MessageHumanity;
import targoss.hardcorealchemy.network.MessageKillCount;
import targoss.hardcorealchemy.network.MessageMorphState;

public class ListenerCapabilities extends HardcoreAlchemyListener {
    @CapabilityInject(ICapabilityHumanity.class)
    public static final Capability<ICapabilityHumanity> HUMANITY_CAPABILITY = null;
    @CapabilityInject(ICapabilityKillCount.class)
    public static final Capability<ICapabilityKillCount> KILL_COUNT_CAPABILITY = null;
    @CapabilityInject(ICapabilityInstinct.class)
    public static final Capability<ICapabilityInstinct> INSTINCT_CAPABILITY = null;
    @CapabilityInject(ICapabilityMorphState.class)
    public static final Capability<ICapabilityMorphState> MORPH_STATE_CAPABILITY = null;
    
    @Override
    public void registerCapabilities(CapabilityManager manager, CapUtil.Manager virtualManager) {
        manager.register(ICapabilityInstinct.class, new StorageInstinct(), CapabilityInstinct.class);
    }

    @SubscribeEvent
    public void onAttachPlayerCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof EntityPlayer) {
            event.addCapability(CapabilityInstinct.RESOURCE_LOCATION, new ProviderInstinct());
            AbstractAttributeMap attributeMap = ((EntityPlayer)event.getObject()).getAttributeMap();
            if (attributeMap.getAttributeInstance(ICapabilityInstinct.MAX_INSTINCT) == null) {
                attributeMap.registerAttribute(ICapabilityInstinct.MAX_INSTINCT);
            }
        }
    }
    
    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        EntityPlayer oldPlayer = event.getOriginal();
        EntityPlayer newPlayer = event.getEntityPlayer();
        if (!event.isWasDeath()) {
            CapUtil.copyOldToNew(INSTINCT_CAPABILITY, oldPlayer, newPlayer);
        }
        if (!event.isWasDeath() || Metamorph.keepMorphs.get()) {
            CapUtil.copyOldToNew(KILL_COUNT_CAPABILITY, oldPlayer, newPlayer);
            CapUtil.copyOldToNew(HUMANITY_CAPABILITY, oldPlayer, newPlayer);
            ListenerPlayerMorphs.updateMaxHumanity(newPlayer);
        }
    }
    
    @SubscribeEvent
    public void onPlayerLogin(PlayerLoggedInEvent event) {
        if (event.player.world.isRemote) {
            return;
        }
        
        syncFullPlayerCapabilities((EntityPlayerMP)(event.player));
    }
    
    @SubscribeEvent
    public void onPlayerRespawnMP(PlayerRespawnEvent event) {
        if (event.player.world.isRemote) {
            return;
        }
        
        syncFullPlayerCapabilities((EntityPlayerMP)(event.player));
    }

    @SubscribeEvent
    public void onPlayerEnterDimension(PlayerChangedDimensionEvent event) {
        if (event.player.world.isRemote) {
            return;
        }
        
        syncFullPlayerCapabilities((EntityPlayerMP)(event.player));
    }
    
    public void syncFullPlayerCapabilities(EntityPlayerMP player) {
        ICapabilityHumanity humanity = player.getCapability(HUMANITY_CAPABILITY, null);
        if (humanity != null) {
            HardcoreAlchemy.proxy.messenger.sendTo(new MessageHumanity(humanity, true), (EntityPlayerMP)player);
        }
        ICapabilityKillCount killCount = player.getCapability(KILL_COUNT_CAPABILITY, null);
        if (killCount != null) {
            HardcoreAlchemy.proxy.messenger.sendTo(new MessageKillCount(killCount), (EntityPlayerMP)player);
        }
        ICapabilityInstinct instinct = player.getCapability(INSTINCT_CAPABILITY, null);
        if (instinct != null) {
            HardcoreAlchemyCreatures.proxy.messenger.sendTo(new MessageInstinct(instinct), (EntityPlayerMP)player);
        }
        ICapabilityMorphState morphState = player.getCapability(MORPH_STATE_CAPABILITY, null);
        if (morphState != null) {
            HardcoreAlchemy.proxy.messenger.sendTo(new MessageMorphState(morphState), (EntityPlayerMP)player);
        }
    }
}
