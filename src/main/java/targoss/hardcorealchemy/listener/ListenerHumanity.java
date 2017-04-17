package targoss.hardcorealchemy.listener;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import targoss.hardcorealchemy.capabilities.CapabilityHumanity;
import targoss.hardcorealchemy.capabilities.ICapabilityHumanity;
import targoss.hardcorealchemy.capabilities.ProviderHumanity;
import targoss.hardcorealchemy.capabilities.ProviderKillCount;

public class ListenerHumanity {
    @CapabilityInject(ICapabilityHumanity.class)
    public static final Capability<ICapabilityHumanity> HUMANITY_CAPABILITY = null;
    public static final ResourceLocation HUMANITY_RESOURCE_LOCATION = CapabilityHumanity.RESOURCE_LOCATION;
    
    @SubscribeEvent
    public void onAttachCapability(AttachCapabilitiesEvent<Entity> event) {
        Entity entity = event.getObject();
        if (!(entity instanceof EntityPlayer)) {
            return;
        }
        event.addCapability(HUMANITY_RESOURCE_LOCATION, new ProviderHumanity());
    }
}
