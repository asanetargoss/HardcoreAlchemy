package targoss.hardcorealchemy.tweaks.listener;

import static targoss.hardcorealchemy.tweaks.item.Items.HEART_TEARS;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import targoss.hardcorealchemy.listener.HardcoreAlchemyListener;
import targoss.hardcorealchemy.tweaks.capability.hearts.ICapabilityHearts;

public class ListenerHeartsSacrificed extends HardcoreAlchemyListener {
    @CapabilityInject(ICapabilityHearts.class)
    public static final Capability<ICapabilityHearts> HEARTS_CAPABILITY = null;
    
    @SubscribeEvent
    public void onPlayerTakeFallDamage(LivingFallEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (!(entity instanceof EntityPlayer)) {
            return;
        }
        EntityPlayer player = (EntityPlayer)entity;
        ICapabilityHearts hearts = player.getCapability(HEARTS_CAPABILITY, null);
        if (hearts == null) {
            return;
        }
        if (!hearts.getSacrificed().contains(HEART_TEARS)) {
            return;
        }
        event.setCanceled(true);
    }
}
