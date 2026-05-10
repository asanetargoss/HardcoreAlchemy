package targoss.hardcorealchemy.tweaks.listener;

import static targoss.hardcorealchemy.tweaks.item.Items.HEART_TEARS;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import targoss.hardcorealchemy.listener.HardcoreAlchemyListener;
import targoss.hardcorealchemy.tweaks.capability.hearts.ICapabilityHearts;
import targoss.hardcorealchemy.tweaks.entity.ai.AIEndermanSeenByPlayer;
import targoss.hardcorealchemy.util.EntityUtil;

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
    
    public static Set<EntityUtil.AIReplacer> aiReplacers = new HashSet<>();
    
    static {
        // Makes Endermen ignore the player if they have sacrificed the Heart of Void
        aiReplacers.add(new EntityUtil.AIReplacer(EntityEnderman.AIFindPlayer.class, AIEndermanSeenByPlayer.class));
    }
    
    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof EntityLiving) {
            EntityLiving entityLiving = (EntityLiving)entity;
            for (EntityUtil.AIReplacer aiReplacer : aiReplacers) {
                EntityUtil.wrapReplaceAttackAI(entityLiving, aiReplacer);
            }
        }
    }
}
