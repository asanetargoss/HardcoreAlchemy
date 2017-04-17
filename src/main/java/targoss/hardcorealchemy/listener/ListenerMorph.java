package targoss.hardcorealchemy.listener;

import java.util.HashMap;
import java.util.Map;

import mchorse.metamorph.api.events.MorphEvent;
import mchorse.metamorph.api.events.SpawnGhostEvent;
import mchorse.metamorph.api.morphs.AbstractMorph;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.capabilities.CapabilityHumanity;
import targoss.hardcorealchemy.capabilities.CapabilityKillCount;
import targoss.hardcorealchemy.capabilities.ICapabilityHumanity;
import targoss.hardcorealchemy.capabilities.ICapabilityKillCount;
import targoss.hardcorealchemy.capabilities.ProviderHumanity;
import targoss.hardcorealchemy.capabilities.ProviderKillCount;

public class ListenerMorph {
	
	public static Map<String, Integer> mapRequiredKills = new HashMap<String, Integer>();
	
	@CapabilityInject(ICapabilityKillCount.class)
    public static final Capability<ICapabilityKillCount> KILL_COUNT_CAPABILITY = null;
	public static final ResourceLocation KILL_COUNT_RESOURCE_LOCATION = CapabilityKillCount.RESOURCE_LOCATION;
	
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
	
	public ListenerMorph() { }
	
	@SubscribeEvent
	public void onAttachCapability(AttachCapabilitiesEvent<Entity> event) {
	    Entity entity = event.getObject();
	    if (!(entity instanceof EntityPlayer)) {
	        return;
	    }
	    event.addCapability(KILL_COUNT_RESOURCE_LOCATION, new ProviderKillCount());
	}
	
	@SubscribeEvent
	public void onSpawnGhost(SpawnGhostEvent event) {
	    // Get morph
		AbstractMorph morph = event.morph;
		if (morph == null) {
		    return;
		}		
		String morphName = morph.name;
		// Get player capability for kill count
		if (KILL_COUNT_CAPABILITY == null) {
		    return;
		}
		ICapabilityKillCount killCount = event.player.getCapability(KILL_COUNT_CAPABILITY, null);
		if (killCount == null) {
		    return;
		}
		// How many kills?
		Integer requiredKills = (mapRequiredKills.get(morphName));
		if (requiredKills == null) {
		    // Set a sane default
		    mapRequiredKills.put(morphName, 5);
		}
		killCount.addKill(morph.name);
	    int timesKilled = killCount.getNumKills(morph.name);
	    // The player has to kill the mob requiredKills times to make the ghost spawn
	    if (timesKilled % requiredKills != 0) {
	        event.setCanceled(true);
	    }
	}
}
