package targoss.hardcorealchemy.listener;

import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import targoss.hardcorealchemy.capability.humanity.ICapabilityHumanity;
import targoss.hardcorealchemy.config.Configs;
import targoss.hardcorealchemy.network.MessageHumanity;
import targoss.hardcorealchemy.network.MessageMagic;
import targoss.hardcorealchemy.network.PacketHandler;

/**
 * It is convention in this mod to not create capabilities or do any capability
 * calculations on the client side. This event listener is responsible for sending
 * the little data that the client needs to function correctly.
 */
public class ListenerPacketUpdatePlayer extends ConfiguredListener {
    public ListenerPacketUpdatePlayer(Configs configs) {
        super(configs);
    }
    
    @CapabilityInject(ICapabilityHumanity.class)
    public static final Capability<ICapabilityHumanity> HUMANITY_CAPABILITY = null;
    public static final IAttribute MAX_HUMANITY = ICapabilityHumanity.MAX_HUMANITY;
    
    private static final int PLAYER_UPDATE_TICKS = 7;
    
    @SubscribeEvent
    public void onPlayerTickMP(TickEvent.PlayerTickEvent event) {
        if (event.player.worldObj.isRemote) {
            return;
        }
        EntityPlayerMP player = (EntityPlayerMP)(event.player);
        if (event.phase == Phase.END) {
            sendPlayerUpdatePacket(player);
        }
    }
    
    //TODO: fix packets being sent every tick when humanity is null (but should we use client-side prediction?)
    public void sendPlayerUpdatePacket(EntityPlayerMP player) {
        ICapabilityHumanity capabilityHumanity = player.getCapability(HUMANITY_CAPABILITY, null);
        if (capabilityHumanity == null) {
            PacketHandler.INSTANCE.sendTo(new MessageHumanity(false, 0, 0), player);
            PacketHandler.INSTANCE.sendTo(new MessageMagic(true), player);
            return;
        }
        
        int humanityTick = capabilityHumanity.getTick();
        if (humanityTick >= PLAYER_UPDATE_TICKS) {
            capabilityHumanity.setTick(0);
            
            // Compensate for the max humanity modifier not existing
            IAttributeInstance humanityInstance = player.getAttributeMap().getAttributeInstance(MAX_HUMANITY);
            double maxHumanity;
            if (humanityInstance != null) {
                maxHumanity = humanityInstance.getAttributeValue();
            }
            else {
                maxHumanity = 20.0D;
            }
            
            // Update client humanity GUI
            PacketHandler.INSTANCE.sendTo(
                    new MessageHumanity(capabilityHumanity.shouldDisplayHumanity(),
                            capabilityHumanity.getHumanity(),
                            maxHumanity),
                    player);
            
            // Update client magic usage prediction
            PacketHandler.INSTANCE.sendTo(
                    new MessageMagic(capabilityHumanity.canUseHighMagic()),
                    player);
            
        }
        else {
            capabilityHumanity.setTick(humanityTick + 1);
        }
    }
}
