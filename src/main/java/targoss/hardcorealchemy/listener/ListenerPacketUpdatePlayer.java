package targoss.hardcorealchemy.listener;

import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
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
        if (event.player.worldObj.isRemote ||
                event.phase != Phase.END ||
                event.player.ticksExisted % PLAYER_UPDATE_TICKS != 0) {
            return;
        }
        
        sendPlayerUpdatePacket((EntityPlayerMP)(event.player));
    }
    
    public void sendPlayerUpdatePacket(EntityPlayerMP player) {
        ICapabilityHumanity capabilityHumanity = player.getCapability(HUMANITY_CAPABILITY, null);
        
        if (capabilityHumanity == null) {
            PacketHandler.INSTANCE.sendTo(new MessageHumanity(false, 0, 0), player);
            PacketHandler.INSTANCE.sendTo(new MessageMagic(true), player);
        }
        else {
            IAttributeInstance humanityInstance = player.getAttributeMap().getAttributeInstance(MAX_HUMANITY);
            double maxHumanity;
            if (humanityInstance != null) {
                maxHumanity = humanityInstance.getAttributeValue();
            }
            else {
                // Compensate for the max humanity modifier not existing
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
    }
}
