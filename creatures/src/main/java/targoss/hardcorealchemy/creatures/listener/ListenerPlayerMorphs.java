/*
 * Copyright 2017-2023 asanetargoss
 *
 * This file is part of Hardcore Alchemy Creatures.
 *
 * Hardcore Alchemy Creatures is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Creatures is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Creatures. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.creatures.listener;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import mchorse.metamorph.api.events.AcquireMorphEvent;
import mchorse.metamorph.api.events.RegisterBlacklistEvent;
import mchorse.metamorph.api.events.SpawnGhostEvent;
import mchorse.metamorph.api.morphs.AbstractMorph;
import mchorse.metamorph.api.morphs.EntityMorph;
import mchorse.metamorph.capabilities.morphing.IMorphing;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import targoss.hardcorealchemy.HardcoreAlchemyCore;
import targoss.hardcorealchemy.capability.humanity.ICapabilityHumanity;
import targoss.hardcorealchemy.creatures.HardcoreAlchemyCreatures;
import targoss.hardcorealchemy.creatures.capability.killcount.CapabilityKillCount;
import targoss.hardcorealchemy.creatures.capability.killcount.ICapabilityKillCount;
import targoss.hardcorealchemy.creatures.capability.killcount.ProviderKillCount;
import targoss.hardcorealchemy.creatures.network.MessageHumanity;
import targoss.hardcorealchemy.creatures.network.MessageMaxHumanity;
import targoss.hardcorealchemy.listener.HardcoreAlchemyListener;
import targoss.hardcorealchemy.util.MobLists;

public class ListenerPlayerMorphs extends HardcoreAlchemyListener {
    public static final Map<String, Integer> mapRequiredKills = Collections.synchronizedMap(new HashMap<String, Integer>());
    /* Required morph counts for each max humanity upgrade.
     * +2 humanity per goal reached, up to a maximum of
     * 20 max humanity. Since the starting max humanity is
     * 6, this means 7 upgrades.
     */
    private static int[] morphThresholds = new int[]{3,6,9,12,15,20,25};

    @CapabilityInject(ICapabilityKillCount.class)
    public static final Capability<ICapabilityKillCount> KILL_COUNT_CAPABILITY = null;
    public static final ResourceLocation KILL_COUNT_RESOURCE_LOCATION = CapabilityKillCount.RESOURCE_LOCATION;
    @CapabilityInject(ICapabilityHumanity.class)
    public static final Capability<ICapabilityHumanity> HUMANITY_CAPABILITY = null;
    public static final UUID MORPH_COUNT_BONUS = UUID.fromString("aaca31f4-1778-4c54-9a2f-02c95912b012");
    public static final String MORPH_COUNT_BONUS_NAME = HardcoreAlchemyCore.MOD_ID + ":morphCountBonus";

    // The capability from Metamorph itself
    @CapabilityInject(IMorphing.class)
    public static final Capability<IMorphing> MORPHING_CAPABILITY = null;

    static {
        // Decide how many times a mob needs to be killed to get its morph
        // More valuable/powerful mobs have higher kill requirements
        // More rare/hard-to-access mobs have lower kill requirements to balance time spent searching
        
        // Defaults
        for (String mob : MobLists.getLandAnimals()) {
            mapRequiredKills.put(mob, 6);
        }
        for (String mob : MobLists.getEntityTameables()) {
            mapRequiredKills.put(mob, 12);
        }
        for (String mob : MobLists.getNightMobs()) {
            mapRequiredKills.put(mob, 24);
        }
        for (String mob : MobLists.getNetherMobs()) {
            mapRequiredKills.put(mob, 20);
        }
        for (String mob : MobLists.getAuraMobs()) {
            mapRequiredKills.put(mob, 16);
        }
        for (String mob : MobLists.getTaintMobs()) {
            mapRequiredKills.put(mob, 24);
        }
        for (String mob : MobLists.getEldritchMobs()) {
            mapRequiredKills.put(mob, 26);
        }
        for (String mob : MobLists.getTrollMobs()) {
            mapRequiredKills.put(mob, 6);
        }
        
        // Custom
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
        mapRequiredKills.put("CaveSpider", 14);
        mapRequiredKills.put("Zombie", 12);
        mapRequiredKills.put("Skeleton", 24);
        mapRequiredKills.put("Creeper", 12);
        mapRequiredKills.put("Enderman", 20);
        mapRequiredKills.put("Guardian", 8);
        // Nether monsters
        mapRequiredKills.put("LavaSlime", 14);
        mapRequiredKills.put("Blaze", 40);
        mapRequiredKills.put("Ghast", 8);
        // Ender Zoo
        mapRequiredKills.put("EnderZoo.Owl", 25);
    }
    
    /**
     * Prevent acquiring human-like morphs, bosses, and stuff
     * that shouldn't be morphs in the first place
     */
    @SubscribeEvent
    public void onRegisterMorphBlacklist(RegisterBlacklistEvent event) {
        Set<String> morphBlacklist = event.blacklist;
        
        morphBlacklist.addAll(MobLists.getHumans());
        morphBlacklist.addAll(MobLists.getNonMobs());
        morphBlacklist.addAll(MobLists.getBosses());
    }

    @SubscribeEvent
    public void onAttachCapability(AttachCapabilitiesEvent<Entity> event) {
        Entity entity = event.getObject();
        if (!(entity instanceof EntityPlayer)) {
            return;
        }
        event.addCapability(KILL_COUNT_RESOURCE_LOCATION, new ProviderKillCount());
    }

    @SubscribeEvent
	public void onSpawnGhost(SpawnGhostEvent.Pre event) {
	    // Get morph. If it's not an entity morph ghost, it's not really a kill count.
		AbstractMorph morph = event.morph;
		if (morph == null || !(morph instanceof EntityMorph)) {
		    return;
		}
		String morphName = morph.name;
		if (morphName == null || morphName.equals("")) {
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
    
    /** On player join world, add mastered kills for all acquired morphs */
    @SubscribeEvent
    public void onPlayerJoinWorld(EntityJoinWorldEvent event) {
        Entity entity = event.getEntity();
        if (entity == null || entity.world.isRemote) {
            return;
        }
        if (!(entity instanceof EntityPlayer)) {
            return;
        }
        EntityPlayer player = (EntityPlayer)entity;
        ListenerPlayerKillMastery.recalculateMasteredKills(player);
        updateMaxHumanity(player, false);
    }
    
    // TODO: Recalculate kills and max humanity when the player loses a morph via the "Remove" button in the survival morph menu
    
    @SubscribeEvent
    public void onPlayerAcquireMorph(AcquireMorphEvent.Post event) {
        ListenerPlayerKillMastery.addMasteredKill(event);
        updateMaxHumanity(event.player, true);
    }
    
    @SubscribeEvent
    public void onPlayerChangeDimension(PlayerChangedDimensionEvent event) {
        // Fix removal of the humanity modifier when switching dimensions
        // I'm not sure why Minecraft insists on clearing modifiers on change dimension,
        // but no use arguing with it.
        updateMaxHumanity(event.player, false);
    }
    
    public static void updateMaxHumanity(EntityPlayer player, boolean isUpgrade) {
        double bonusCap = 0.0D;

        // Count all the player's acquired entity morphs (excluding variants and freebie mobs)
        ICapabilityKillCount killCount = player.getCapability(KILL_COUNT_CAPABILITY, null);
        if (killCount != null) {
            Set<String> masteredKills = killCount.getMasteredKills();
            Set<String> freebieMobs = MobLists.getFreebieMobs();
            int count = masteredKills.size();
            for (String freebieMob : freebieMobs) {
                if (masteredKills.contains(freebieMob)) {
                    --count;
                }
            }
            for (int threshold : morphThresholds) {
                if (threshold <= count) {
                    bonusCap += 2;
                }
                else {
                    break;
                }
            }
        }

        IAttributeInstance maxHumanity = player.getEntityAttribute(ICapabilityHumanity.MAX_HUMANITY);

        double oldCap = maxHumanity.getAttributeValue();

        AttributeModifier existingModifier = maxHumanity.getModifier(MORPH_COUNT_BONUS);
        if (existingModifier != null) {
            maxHumanity.removeModifier(existingModifier);
        }
        maxHumanity.applyModifier(new AttributeModifier(MORPH_COUNT_BONUS, MORPH_COUNT_BONUS_NAME, bonusCap, 0));
        
        double newCap = maxHumanity.getAttributeValue();
        
        if (!player.world.isRemote) {
            HardcoreAlchemyCreatures.proxy.messenger.sendTo(new MessageMaxHumanity(), (EntityPlayerMP)player);
        }
        
        if (isUpgrade && newCap > oldCap) {
            ICapabilityHumanity humanity = player.getCapability(HUMANITY_CAPABILITY, null);
            if (humanity != null) {
                humanity.setHumanity(humanity.getHumanity() + newCap - oldCap);
                if (!player.world.isRemote) {
                    HardcoreAlchemyCreatures.proxy.messenger.sendTo(new MessageHumanity(humanity, false), (EntityPlayerMP)player);
                }
            }
        }
    }
}
