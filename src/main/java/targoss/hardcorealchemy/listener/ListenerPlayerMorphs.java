/*
 * Copyright 2017-2018 asanetargoss
 * 
 * This file is part of Hardcore Alchemy.
 * 
 * Hardcore Alchemy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation version 3 of the License.
 * 
 * Hardcore Alchemy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Hardcore Alchemy.  If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.listener;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import mchorse.metamorph.Metamorph;
import mchorse.metamorph.api.events.AcquireMorphEvent;
import mchorse.metamorph.api.events.MorphEvent;
import mchorse.metamorph.api.events.RegisterBlacklistEvent;
import mchorse.metamorph.api.events.SpawnGhostEvent;
import mchorse.metamorph.api.morphs.AbstractMorph;
import mchorse.metamorph.api.morphs.EntityMorph;
import mchorse.metamorph.capabilities.morphing.IMorphing;
import mchorse.metamorph.capabilities.morphing.Morphing;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.Clone;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.capability.CapUtil;
import targoss.hardcorealchemy.capability.humanity.ICapabilityHumanity;
import targoss.hardcorealchemy.capability.killcount.CapabilityKillCount;
import targoss.hardcorealchemy.capability.killcount.ICapabilityKillCount;
import targoss.hardcorealchemy.capability.killcount.ProviderKillCount;
import targoss.hardcorealchemy.config.Configs;
import targoss.hardcorealchemy.util.Chat;
import targoss.hardcorealchemy.util.MobLists;
import targoss.hardcorealchemy.util.MorphState;

public class ListenerPlayerMorphs extends ConfiguredListener {
    public ListenerPlayerMorphs(Configs configs) {
        super(configs);
    }

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
    public static final String MORPH_COUNT_BONUS_NAME = HardcoreAlchemy.MOD_ID + ":morphCountBonus";

    // The capability from Metamorph itself
    @CapabilityInject(IMorphing.class)
    public static final Capability<IMorphing> MORPHING_CAPABILITY = null;

    static {
        // TODO: add the rest of the morph counts
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
        mapRequiredKills.put("Skeleton", 24);
        mapRequiredKills.put("Creeper", 12);
        mapRequiredKills.put("Enderman", 20);
        mapRequiredKills.put("Guardian", 8);
        // Nether monsters
        mapRequiredKills.put("LavaSlime", 14);
        mapRequiredKills.put("Blaze", 40);
        mapRequiredKills.put("Ghast", 8);
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

    @SubscribeEvent
    public void onPlayerClone(Clone event) {
        if (!event.isWasDeath() || Metamorph.proxy.config.keep_morphs) {
            // Keep kill count if morphs are kept
            EntityPlayer player = event.getEntityPlayer();
            EntityPlayer playerOld = event.getOriginal();
            CapUtil.copyOldToNew(KILL_COUNT_CAPABILITY, playerOld, player);
        }
    }
    
    public static void updateMaxHumanity(EntityPlayer player) {
        double bonusCap = 0.0D;
        
        IMorphing morphing = Morphing.get(player);
        if (morphing != null) {
            // Count all the player's acquired entity morphs (screen out variants)
            Set<String> creatureNames = new HashSet<>();
            for (AbstractMorph morph : morphing.getAcquiredMorphs()) {
                if (morph instanceof EntityMorph) {
                    creatureNames.add(morph.name);
                }
            }
            
            int count = creatureNames.size();
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
        AttributeModifier existingModifier = maxHumanity.getModifier(MORPH_COUNT_BONUS);
        if (existingModifier != null) {
            maxHumanity.removeModifier(existingModifier);
        }
        maxHumanity.applyModifier(new AttributeModifier(MORPH_COUNT_BONUS, MORPH_COUNT_BONUS_NAME, bonusCap, 0));
    }
    
    @SubscribeEvent
    public void onPlayerAcquireMorph(AcquireMorphEvent.Post event) {
        updateMaxHumanity(event.player);
    }
    
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        EntityPlayer player = event.player;
        
        double oldCap = player.getEntityAttribute(ICapabilityHumanity.MAX_HUMANITY).getAttributeValue();
        updateMaxHumanity(player);
        double newCap = player.getEntityAttribute(ICapabilityHumanity.MAX_HUMANITY).getAttributeValue();
        ICapabilityHumanity humanity = player.getCapability(HUMANITY_CAPABILITY, null);
        if (humanity != null && newCap > oldCap) {
            humanity.setHumanity(humanity.getHumanity() + newCap - oldCap);
        }
    }
    
    private static void updatePlayerSize(EntityPlayer player, AbstractMorph morph) {
        if (morph == null) {
            player.width = MorphState.PLAYER_WIDTH_HEIGHT[0];
            player.height = MorphState.PLAYER_WIDTH_HEIGHT[1];
            player.eyeHeight = player.height * 0.9F;
        }
        else {
            if ((morph instanceof EntityMorph)) {
                EntityLivingBase morphEntity = ((EntityMorph) morph).getEntity(player.world);
                // EntityMorph.updateSize() via reflection
                try {
                    Method updateSizeMethod = EntityMorph.class.getDeclaredMethod(
                            "updateSize", EntityLivingBase.class, float.class, float.class);
                    updateSizeMethod.setAccessible(true);
                    updateSizeMethod.invoke((EntityMorph)morph, player, morphEntity.width, morphEntity.height);
                } catch (NoSuchMethodException | SecurityException | IllegalAccessException |
                        IllegalArgumentException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
            // Don't know how to handle other morphs right now
        }
    }
    
    /**
     * Prevents unexpected suffocation damage by canceling
     * morphing if there is insufficient space.
     */
    @SubscribeEvent
    public void onMorphCheckSpace(MorphEvent.Pre event) {
        EntityPlayer player = event.player;
        
        if (player.noClip) {
            // No suffocation damage to worry about
            return;
        }
        if (event.force) {
            // Don't want to prevent a forced morph
            //TODO: Temporary suffocation immunity
            return;
        }
        if (player.isEntityInsideOpaqueBlock()) {
            // No point in preventing the player from morphing if they're already suffocating
            return;
        }
        
        updatePlayerSize(player, event.morph);
        
        if (player.isEntityInsideOpaqueBlock()) {
            event.setCanceled(true);
            if (!player.world.isRemote) {
                Chat.notify((EntityPlayerMP)player, new TextComponentTranslation("hardcorealchemy.morph.suffocation_risk"));
            }
        }
        
        updatePlayerSize(player, Morphing.get(player).getCurrentMorph());
    }
}
