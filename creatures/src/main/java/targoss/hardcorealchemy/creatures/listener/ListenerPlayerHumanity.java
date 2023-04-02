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

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import mchorse.metamorph.api.events.MorphEvent;
import mchorse.metamorph.api.events.SpawnGhostEvent;
import mchorse.metamorph.api.morphs.AbstractMorph;
import mchorse.metamorph.capabilities.morphing.IMorphing;
import mcp.mobius.waila.api.event.WailaRenderEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import targoss.hardcorealchemy.ModState;
import targoss.hardcorealchemy.capability.humanity.CapabilityHumanity;
import targoss.hardcorealchemy.capability.humanity.ICapabilityHumanity;
import targoss.hardcorealchemy.capability.humanity.MorphAbilityChangeReason;
import targoss.hardcorealchemy.capability.misc.ICapabilityMisc;
import targoss.hardcorealchemy.creatures.HardcoreAlchemyCreatures;
import targoss.hardcorealchemy.creatures.network.MessageHumanity;
import targoss.hardcorealchemy.creatures.research.Studies;
import targoss.hardcorealchemy.creatures.util.MorphState;
import targoss.hardcorealchemy.listener.HardcoreAlchemyListener;
import targoss.hardcorealchemy.listener.ListenerPlayerResearch;
import targoss.hardcorealchemy.util.Chat;
import targoss.hardcorealchemy.util.InventoryUtil;
import targoss.hardcorealchemy.util.MiscVanilla;
import targoss.hardcorealchemy.util.MorphExtension;

public class ListenerPlayerHumanity extends HardcoreAlchemyListener {
    @CapabilityInject(ICapabilityHumanity.class)
    public static final Capability<ICapabilityHumanity> HUMANITY_CAPABILITY = null;
    public static final ResourceLocation HUMANITY_RESOURCE_LOCATION = CapabilityHumanity.RESOURCE_LOCATION;
    public static final IAttribute MAX_HUMANITY = ICapabilityHumanity.MAX_HUMANITY;
    // Time in ticks between sending humanity value updates over the network
    public static final int HUMANITY_UPDATE_TICKS = 7;

    private Random random = new Random();
    
    // The capability from Metamorph itself
    @CapabilityInject(IMorphing.class)
    public static final Capability<IMorphing> MORPHING_CAPABILITY = null;
    
    @CapabilityInject(ICapabilityMisc.class)
    private static final Capability<ICapabilityMisc> MISC_CAPABILITY = null;

    public static final Set<String> SPELL_ITEMS = new HashSet<>();
    
    static {
        SPELL_ITEMS.add("arsmagica2:spell_component");
        SPELL_ITEMS.add("arsmagica2:spell_staff_magitech");
        SPELL_ITEMS.add("arsmagica2:arcane_spellbook");
        SPELL_ITEMS.add("arsmagica2:spell");
        SPELL_ITEMS.add("arsmagica2:spell_book");
        SPELL_ITEMS.add("alchemicash:Skystone");
        SPELL_ITEMS.add("alchemicash:Skystone2");
        SPELL_ITEMS.add("thaumcraft:caster_basic");
    }
    
    private static Item ROTTEN_FLESH;
    private static Item CHORUS_FRUIT;
    private static Item GOLDEN_APPLE;
    private static Item WITHER_APPLE;
    
    @Override
    public void postInit(FMLPostInitializationEvent event) {
        ROTTEN_FLESH = Item.getByNameOrId("minecraft:rotten_flesh");
        CHORUS_FRUIT = Item.getByNameOrId("minecraft:chorus_fruit");
        GOLDEN_APPLE = Item.getByNameOrId("minecraft:golden_apple");
        WITHER_APPLE = Item.getByNameOrId("adinferos:wither_apple");
        // Any more than this, and I might as well make it a hash map. >.>
    }
    
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == Phase.START) {
            calculateHumanity(event.player);
        }
    }
    
    @SubscribeEvent
    public void onPlayerEat(LivingEntityUseItemEvent.Finish event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof EntityPlayer)) {
            return;
        }
        EntityPlayer player = (EntityPlayer)entity;
        ItemStack itemStack = event.getItem();
        if (InventoryUtil.isEmptyItemStack(itemStack)) {
            return;
        }
        Item item = itemStack.getItem();
        if (item != null && (item == ROTTEN_FLESH || item == CHORUS_FRUIT || item == GOLDEN_APPLE || item == WITHER_APPLE)) {
            IMorphing morphing = player.getCapability(MORPHING_CAPABILITY, null);
            if (morphing == null) {
                return;
            }
            ICapabilityHumanity capabilityHumanity = player.getCapability(HUMANITY_CAPABILITY, null);
            if (capabilityHumanity == null || !capabilityHumanity.canMorph()) {
                return;
            }
            IAttributeInstance maxHumanity = player.getEntityAttribute(MAX_HUMANITY);
            if (maxHumanity == null) {
                return;
            }
            
            double newHumanity = capabilityHumanity.getHumanity();
            double newMagicInhibition = capabilityHumanity.getMagicInhibition();
            if (item == GOLDEN_APPLE) {
                // A testing item, but I guess it's balanced enough for regular gameplay
                newHumanity = MathHelper.clamp(newHumanity+capabilityHumanity.getHumanityNMinutesLeft(3), 0.0D, maxHumanity.getAttributeValue());
                newMagicInhibition = MathHelper.clamp(newMagicInhibition-0.25D, 0.0D, 1.0D+maxHumanity.getAttributeValue());
            }
            else if (item == CHORUS_FRUIT) {
                newHumanity = MathHelper.clamp(newHumanity-1.0D, 0.0D, maxHumanity.getAttributeValue());
                newMagicInhibition = MathHelper.clamp(newMagicInhibition+capabilityHumanity.getHumanityNMinutesLeft(2), 0.0D, 1.0D+maxHumanity.getAttributeValue());
            }
            else {
                newHumanity = MathHelper.clamp(newHumanity-1.0D, 0.0D, maxHumanity.getAttributeValue());
            }
            capabilityHumanity.setHumanity(newHumanity);
            capabilityHumanity.setMagicInhibition(newMagicInhibition);
            
            if (!player.world.isRemote) {
                HardcoreAlchemyCreatures.proxy.messenger.sendTo(new MessageHumanity(capabilityHumanity, false), (EntityPlayerMP)player);
            }
            
            if (newHumanity == 0) {
                if (!player.world.isRemote) {
                    if (item == ROTTEN_FLESH) {
                        // If you are already in a morph, then congrats, you get to keep that morph!
                        if (morphing.getCurrentMorph() == null) {
                            // Uh oh, you're a zombie now!
                            MorphState.forceForm(coreConfigs, player, MorphAbilityChangeReason.LOST_HUMANITY, "Zombie");
                        }
                        else {
                            MorphState.forceForm(coreConfigs, player, MorphAbilityChangeReason.LOST_HUMANITY, morphing.getCurrentMorph());
                        }
                    }
                    else if (item == CHORUS_FRUIT) {
                        // Uh oh, you're an enderman now!
                        MorphState.forceForm(coreConfigs, player, MorphAbilityChangeReason.LOST_HUMANITY, "Enderman");
                    }
                    else if (item == WITHER_APPLE) {
                        // Uh oh, you're a wither skeleton now!
                        NBTTagCompound nbt = new NBTTagCompound();
                        NBTTagCompound nbtEntityData = new NBTTagCompound();
                        nbt.setTag("EntityData", nbtEntityData);
                        nbtEntityData.setByte("SkeletonType", (byte)1);
                        MorphState.forceForm(coreConfigs, player, MorphAbilityChangeReason.LOST_HUMANITY, "Skeleton", nbt);
                    }
                }
            }
        }
    }
    
    @SubscribeEvent
    public void onPlayerMorph(MorphEvent.Pre event) {
        if (event.force || event.isCanceled()) {
            return;
        }
        
        EntityPlayer player = event.player;
        if (player.world.isRemote) {
            return;
        }
        
        ICapabilityHumanity capabilityHumanity = player.getCapability(HUMANITY_CAPABILITY, null);
        if (capabilityHumanity == null) {
            return;
        }
        if (!capabilityHumanity.canMorphRightNow()) {
            event.setCanceled(true);
            Chat.message(Chat.Type.NOTIFY, (EntityPlayerMP)player, capabilityHumanity.explainWhyCantMorph());
        }
    }
    
    @SubscribeEvent
    public void onPlayerPostMorph(MorphEvent.Post event) {
        // Morphing costs up to one minute of morph time unless you're morphing back into a player
        // A player must be careful not to morph when they are too weak
        EntityPlayer player = event.player;
        if (!event.isDemorphing() && !event.force && !player.world.isRemote) {
            ICapabilityHumanity capabilityHumanity = player.getCapability(HUMANITY_CAPABILITY, null);
            if (capabilityHumanity == null) {
                return;
            }
            double humanityLost = random.nextDouble() * capabilityHumanity.getHumanityNMinutesLeft(1);
            double morphReducedHumanity = capabilityHumanity.getHumanity() - humanityLost;
            if (morphReducedHumanity < 0) morphReducedHumanity = 0;
            capabilityHumanity.setHumanity(morphReducedHumanity);
            // Do the same with magic inhibition, so only casting a spell can truly get you stuck
            double morphReducedMagicInhibition = capabilityHumanity.getMagicInhibition() - humanityLost;
            if (morphReducedMagicInhibition < 0) morphReducedMagicInhibition = 0;
            capabilityHumanity.setMagicInhibition(morphReducedMagicInhibition);
            HardcoreAlchemyCreatures.proxy.messenger.sendTo(new MessageHumanity(capabilityHumanity, false), (EntityPlayerMP)player);
        }
    }
    
    @SubscribeEvent
    public void onSpawnMorphPickup(SpawnGhostEvent.Pre event) {
        ICapabilityHumanity capabilityHumanity = event.player.getCapability(HUMANITY_CAPABILITY, null);
        if (capabilityHumanity == null) {
            return;
        }
        if (!capabilityHumanity.shouldDisplayHumanity()) {
            event.setCanceled(true);
        }
    }
    
    private void calculateHumanity(EntityPlayer player) {
        IAttributeInstance maxHumanity = player.getEntityAttribute(MAX_HUMANITY);
        if (maxHumanity == null) {
            return;
        }
        ICapabilityHumanity capabilityHumanity = player.getCapability(HUMANITY_CAPABILITY, null);
        if (capabilityHumanity == null) {
            return;
        }
        // If the player has a humanity bar, then their humanity and magicInhibition can change
        if (capabilityHumanity.shouldDisplayHumanity()) {
            double oldHumanity = capabilityHumanity.getLastHumanity();
            double newHumanity = capabilityHumanity.getHumanity();
            // Always reduce magic inhibition at the rate of humanity loss
            double newMagicInhibition = capabilityHumanity.getMagicInhibition();
            newMagicInhibition -= capabilityHumanity.getHumanityLossRate();
            newMagicInhibition = MathHelper.clamp(newMagicInhibition, 0.0D, 1.0D+maxHumanity.getAttributeValue());
            capabilityHumanity.setMagicInhibition(newMagicInhibition);
            // Are we in a morph? (check if the player's AbstractMorph is not null)
            IMorphing morphing = player.getCapability(MORPHING_CAPABILITY, null);
            if (morphing.isMorphed()) {
                // Drain humanity when the player is voluntarily in a morph
                newHumanity = MathHelper.clamp(newHumanity-capabilityHumanity.getHumanityLossRate(), 0.0D, maxHumanity.getAttributeValue());
                capabilityHumanity.setHumanity(newHumanity);
                // If humanity reaches zero, make player stuck in a morph
                if (newHumanity <= 0) {
                    if (!player.world.isRemote) {
                        AbstractMorph morph = morphing.getCurrentMorph();
                        if (morph != null) {
                            MorphState.forceForm(coreConfigs, player, MorphAbilityChangeReason.LOST_HUMANITY, morph);
                        }
                        else {
                            // If the player isn't in a morph, give a reasonable default
                            MorphState.forceForm(coreConfigs, player, MorphAbilityChangeReason.LOST_HUMANITY, "Zombie");
                        }
                    }
                }
            }
            else {
                // Restore humanity
                newHumanity = newHumanity + capabilityHumanity.getHumanityGainRate();
                newHumanity = MathHelper.clamp(newHumanity, 0.0D, maxHumanity.getAttributeValue());
            }
            // On client, notify player via chat if humanity reaches a certain threshold or is lost entirely
            // On server side, send a packet as appropriate
            sendHumanityWarnings(player, capabilityHumanity, oldHumanity, newHumanity);
            capabilityHumanity.setHumanity(newHumanity);
            capabilityHumanity.setLastHumanity(newHumanity);
        }
    }
    
    private static final int HUMANITY_WARN_VARIANT_COUNT = 4;
    
    private void sendHumanityWarnings(EntityPlayer player, ICapabilityHumanity capabilityHumanity, double oldHumanity, double newHumanity) {
        // We are only interested if humanity decreases
        if (newHumanity >= oldHumanity) {
            return;
        }
        // If humanity passes a critical threshold, display message (most urgent one first)
        if (newHumanity <= 0) {
            // Display lost humanity message
            Chat.messageSP(Chat.Type.NOTIFY, player, new TextComponentTranslation("hardcorealchemy.humanity.lost"));
        }
        else if (newHumanity <= capabilityHumanity.getHumanityNMinutesLeft(3)) {
            int humanityWarnVariant = 1 + random.nextInt(HUMANITY_WARN_VARIANT_COUNT);
            final double MINUTES_LEFT_1 = capabilityHumanity.getHumanityNMinutesLeft(1);
            final double MINUTES_LEFT_2 = capabilityHumanity.getHumanityNMinutesLeft(2);
            final double MINUTES_LEFT_3 = capabilityHumanity.getHumanityNMinutesLeft(3);
            if (newHumanity <= MINUTES_LEFT_1 && oldHumanity > MINUTES_LEFT_1) {
                // Display 1 minute left message
                if (player.world.isRemote) {
                    Chat.messageSP(Chat.Type.WARN, player, new TextComponentTranslation("hardcorealchemy.humanity.warn3.variant" + humanityWarnVariant));
                }
                else {
                    HardcoreAlchemyCreatures.proxy.messenger.sendTo(new MessageHumanity(capabilityHumanity, false), (EntityPlayerMP)player);
                }
            }
            else if (newHumanity <= MINUTES_LEFT_2 && oldHumanity > MINUTES_LEFT_2) {
                // Display 2 minutes left message
                if (player.world.isRemote) {
                    Chat.messageSP(Chat.Type.NOTIFY, player, new TextComponentTranslation("hardcorealchemy.humanity.warn2.variant" + humanityWarnVariant));
                }
                else {
                    HardcoreAlchemyCreatures.proxy.messenger.sendTo(new MessageHumanity(capabilityHumanity, false), (EntityPlayerMP)player);
                }
            }
            else if (oldHumanity > MINUTES_LEFT_3) {
                // Display 3 minutes left message
                if (player.world.isRemote) {
                    Chat.messageSP(Chat.Type.NOTIFY, player, new TextComponentTranslation("hardcorealchemy.humanity.warn1.variant" + humanityWarnVariant));
                }
                else {
                    HardcoreAlchemyCreatures.proxy.messenger.sendTo(new MessageHumanity(capabilityHumanity, false), (EntityPlayerMP)player);
                }
            }
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerCastSpell(PlayerInteractEvent.RightClickItem event) {
        EntityPlayer player = event.getEntityPlayer();
        ICapabilityHumanity capabilityHumanity = player.getCapability(HUMANITY_CAPABILITY, null);
        if (capabilityHumanity == null || !capabilityHumanity.shouldDisplayHumanity()) {
            return;
        }
        IAttributeInstance maxHumanity = player.getEntityAttribute(MAX_HUMANITY);
        if (maxHumanity == null) {
            return;
        }
        ItemStack itemStack = event.getItemStack();
        if (InventoryUtil.isEmptyItemStack(itemStack)) {
            return;
        }
        if (!SPELL_ITEMS.contains(itemStack.getItem().getRegistryName().toString())) {
            return;
        }
        
        double newMagicInhibition = capabilityHumanity.getMagicInhibition();
        newMagicInhibition += capabilityHumanity.getHumanityNMinutesLeft(1);
        newMagicInhibition = MathHelper.clamp(newMagicInhibition, 0.0D, 1.0D+maxHumanity.getAttributeValue());
        capabilityHumanity.setMagicInhibition(newMagicInhibition);
        if (!player.world.isRemote) {
            HardcoreAlchemyCreatures.proxy.messenger.sendTo(new MessageHumanity(capabilityHumanity, false), (EntityPlayerMP)player);
        }
        
        // Warn the player that using spells (or other select magic items) may interfere with their ability to morph
        IMorphing morphing = player.getCapability(MORPHING_CAPABILITY, null);
        boolean hasAMorph = morphing != null && !morphing.getAcquiredMorphs().isEmpty();
        if (hasAMorph) {
            ListenerPlayerResearch.acquireFactAndSendChatMessage(player, Studies.FACT_MAGIC_INHIBITION_WARNING);
        }
    }

    @Optional.Method(modid = ModState.DISSOLUTION_ID)
    @SubscribeEvent
    public void onPlayerMorphAsGhost(MorphEvent.Pre event) {
        if (MorphExtension.INSTANCE.isGhost(event.player)) {
            // You're a ghost, so being in a morph doesn't really make sense
            event.setCanceled(true);
            if (event.player.world.isRemote) {
                Chat.messageSP(Chat.Type.NOTIFY, event.player, new TextComponentTranslation("hardcorealchemy.morph.disabled.dead"));
            }
        }
    }
    
    /**
     * This event handler fixes state issues caused by the player becoming a ghost (ex: via
     * the Dissolution mod), which are normally cleared automatically when a player dies
     * due to capabilities clearing.
     * 
     * - The player is forced to human form because a player's "soul" is human (lore)
     * - Humanity value is reset
     * - Instincts are cleared so the player isn't bugged about instinct-related needs
     * 
     * Note: The code for death handling is rather ad-hoc right now. Ideally no capabilities
     * would be cleared and data would be manually cleared on a case-by-case basis according
     * to a well-defined player life/afterlife state.
     */
    @SubscribeEvent
    public void onPlayerEnterAfterlife(PlayerRespawnEvent event) {
        EntityPlayer player = event.player;
        if (MorphExtension.INSTANCE.isGhost(player)) {
            // You're a ghost, so being in a morph doesn't really make sense
            MorphState.resetForm(coreConfigs, player);
        }
    }

    /**
     * Don't display WAILA when the player is a ghost, because it
     * covers the spawn compass from Dissolution.
     */
    @Optional.Method(modid=ModState.WAWLA_ID)
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onDisplayWawla(WailaRenderEvent.Pre event) {
        if (MorphExtension.INSTANCE.isGhost(MiscVanilla.getTheMinecraftPlayer())) {
            event.setCanceled(true);
        }
    }
}
