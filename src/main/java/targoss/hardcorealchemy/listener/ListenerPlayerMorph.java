package targoss.hardcorealchemy.listener;

import java.util.HashMap;
import java.util.Map;

import ladysnake.dissolution.common.capabilities.IIncorporealHandler;
import ladysnake.dissolution.common.capabilities.IncorporealDataHandler;
import mchorse.metamorph.Metamorph;
import mchorse.metamorph.api.events.MorphEvent;
import mchorse.metamorph.api.events.SpawnGhostEvent;
import mchorse.metamorph.api.morphs.AbstractMorph;
import mchorse.metamorph.capabilities.morphing.IMorphing;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.Clone;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.capability.CapUtil;
import targoss.hardcorealchemy.capability.humanity.CapabilityHumanity;
import targoss.hardcorealchemy.capability.humanity.ICapabilityHumanity;
import targoss.hardcorealchemy.capability.humanity.ProviderHumanity;
import targoss.hardcorealchemy.capability.killcount.CapabilityKillCount;
import targoss.hardcorealchemy.capability.killcount.ICapabilityKillCount;
import targoss.hardcorealchemy.capability.killcount.ProviderKillCount;

public class ListenerPlayerMorph {
	
	public static Map<String, Integer> mapRequiredKills = new HashMap<String, Integer>();
	
	@CapabilityInject(ICapabilityKillCount.class)
    public static final Capability<ICapabilityKillCount> KILL_COUNT_CAPABILITY = null;
	public static final ResourceLocation KILL_COUNT_RESOURCE_LOCATION = CapabilityKillCount.RESOURCE_LOCATION;
	@CapabilityInject(ICapabilityHumanity.class)
    public static final Capability<ICapabilityHumanity> HUMANITY_CAPABILITY = null;
	
	// The capability from Metamorph itself
    @CapabilityInject(IMorphing.class)
    public static final Capability<IMorphing> MORPHING_CAPABILITY = null;
	
	static {
	    //TODO: add the rest of the morph counts
	    // Overworld animals
	    mapRequiredKills.put("Chicken", 5);
	    mapRequiredKills.put("Pig", 6);
	    mapRequiredKills.put("Sheep", 6);
	    mapRequiredKills.put("Cow", 6);
	    mapRequiredKills.put("MushroomCow", 6);
	    mapRequiredKills.put("PolarBear", 8);
	    mapRequiredKills.put("Bat", 14);
	    mapRequiredKills.put("Squid", 6);
	    mapRequiredKills.put("Rabbit", 5);
	    // Overworld animals (tameable)
	    mapRequiredKills.put("Ozelot", 8);
	    mapRequiredKills.put("EntityHorse", 12);
	    mapRequiredKills.put("Villager", 8);
	    // Overworld monsters
	    mapRequiredKills.put("Slime", 6);
	    mapRequiredKills.put("Silverfish", 8);
	    mapRequiredKills.put("Spider", 8);
	    mapRequiredKills.put("Zombie", 12);
	    mapRequiredKills.put("Skeleton", 12);
	    mapRequiredKills.put("Creeper", 12);
	    mapRequiredKills.put("Enderman", 10);
	    mapRequiredKills.put("Guardian", 8);
	    // Nether monsters
	    mapRequiredKills.put("LavaSlime", 14);
	    mapRequiredKills.put("Blaze", 20);
	    mapRequiredKills.put("Ghast", 8);
	}
	
	public ListenerPlayerMorph() { }
	
	@SubscribeEvent
	public void onAttachCapability(AttachCapabilitiesEvent<Entity> event) {
	    Entity entity = event.getObject();
	    if (!(entity instanceof EntityPlayerMP)) {
	        return;
	    }
	    event.addCapability(KILL_COUNT_RESOURCE_LOCATION, new ProviderKillCount());
	}
	
	@SubscribeEvent
	public void onSpawnGhost(SpawnGhostEvent.Pre event) {
	    // Get morph
		AbstractMorph morph = event.morph;
		if (morph == null) {
		    return;
		}		
		String morphName = morph.name;
		if (morphName == null || morphName.equals("")) {
		    return;
		}
		// Check if the player's humanity permits acquiring morphs
		ICapabilityHumanity humanity = event.player.getCapability(HUMANITY_CAPABILITY, null);
		if (humanity != null && !humanity.canMorph()) {
		    return;
		}
		// Get player capability for kill count
		ICapabilityKillCount killCount = event.player.getCapability(KILL_COUNT_CAPABILITY, null);
		if (killCount == null) {
		    return;
		}
		// How many kills?
		Integer requiredKills = (mapRequiredKills.get(morphName));
		if (requiredKills == null) {
		    // Set a sane default
		    mapRequiredKills.put(morphName, 5);
		    requiredKills = 5;
		}
		killCount.addKill(morphName);
	    int timesKilled = killCount.getNumKills(morphName);
	    // The player has to kill the mob requiredKills times to make the ghost spawn
	    if (timesKilled % requiredKills != 0) {
	        event.setCanceled(true);
	    }
	}
	
	@SubscribeEvent
	public void onPlayerClone(Clone event) {
	    if (event.isWasDeath() && Metamorph.proxy.config.keep_morphs) {
	        // Keep kill count if morphs are kept
	        EntityPlayer player = event.getEntityPlayer();
	        EntityPlayer playerOld = event.getOriginal();
	        CapUtil.copyOldToNew(KILL_COUNT_CAPABILITY, playerOld, player);
	    }
	}
	
	@Optional.Method(modid=HardcoreAlchemy.DISSOLUTION_ID)
	@SubscribeEvent
	public void onPlayerMorphAsGhost(MorphEvent.Pre event) {
	    if (isIncorporeal(event.player) && !event.isDemorphing()) {
    	        // You're a ghost, so being in a morph doesn't really make sense
    	        event.setCanceled(true);
    	        if (event.player instanceof EntityPlayerMP) {
    	            ((EntityPlayerMP)(event.player)).addChatMessage(new TextComponentString(
    	                    "§7§o" + "Your soul shall always be human."
    	                    ));
	        }
	    }
	}
	
	@Optional.Method(modid=HardcoreAlchemy.DISSOLUTION_ID)
	@SubscribeEvent
    public void onPlayerEnterAfterlife(PlayerRespawnEvent event) {
	    EntityPlayer player = event.player;
	    if (isIncorporeal(player)) {
            IMorphing morphing = player.getCapability(MORPHING_CAPABILITY, null);
            if (morphing != null) {
                // You're a ghost, so being in a morph doesn't really make sense
                morphing.demorph(player);
            }
        }
    }
	
	@Optional.Method(modid=HardcoreAlchemy.DISSOLUTION_ID)
	public boolean isIncorporeal(EntityPlayer player) {
	    IIncorporealHandler incorporeal = player.getCapability(IncorporealDataHandler.CAPABILITY_INCORPOREAL, null);
	    if (incorporeal != null && incorporeal.isIncorporeal()) {
	        return true;
	    }
	    return false;
	}
}
