/*
 * Copyright 2017-2023 asanetargoss
 *
 * This file is part of Hardcore Alchemy Magic.
 *
 * Hardcore Alchemy Magic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Magic is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Magic. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.magic.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Sets;

import am2.api.affinity.Affinity;
import am2.api.extensions.IAffinityData;
import am2.extensions.EntityExtension;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import targoss.hardcorealchemy.HardcoreAlchemyCore;
import targoss.hardcorealchemy.ModState;
import targoss.hardcorealchemy.capability.inactive.IInactiveCapabilities;
import targoss.hardcorealchemy.coremod.CoremodHook;
import targoss.hardcorealchemy.event.EventCraftPredict;
import targoss.hardcorealchemy.event.EventTakeStack;
import targoss.hardcorealchemy.listener.HardcoreAlchemyListener;
import targoss.hardcorealchemy.util.Chat;
import targoss.hardcorealchemy.util.InventoryExtension;
import targoss.hardcorealchemy.util.InventoryUtil;
import targoss.hardcorealchemy.util.MiscVanilla;
import targoss.hardcorealchemy.util.MorphExtension;
import thaumcraft.api.capabilities.IPlayerKnowledge;
import thaumcraft.api.capabilities.IPlayerKnowledge.EnumKnowledgeType;
import thaumcraft.api.capabilities.IPlayerWarp;
import thaumcraft.api.items.ItemsTC;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchCategory;
import thaumcraft.common.lib.SoundsTC;

/**
 * Handles capabilities from other magic mods, ex: make Ars Magica
 * affinities only active when the player is capable of using high magic.
 */
public class ListenerPlayerMagicState extends HardcoreAlchemyListener {
    @CapabilityInject(IInactiveCapabilities.class)
    private static final Capability<IInactiveCapabilities> INACTIVE_CAPABILITIES = null;
    
    @CapabilityInject(IAffinityData.class)
    private static final Capability<IAffinityData> AFFINITY_CAPABILITY = null;
    
    @CapabilityInject(IPlayerKnowledge.class)
    private static final Capability<IPlayerKnowledge> THAUMCRAFT_KNOWLEDGE_CAPABILITY = null;
    
    @CapabilityInject(IPlayerWarp.class)
    private static final Capability<IPlayerWarp> WARP_CAPABILITY = null;
    
    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        EntityPlayer player = event.player;
        
        IInactiveCapabilities inactives = player.getCapability(INACTIVE_CAPABILITIES, null);
        if (inactives == null) {
            return;
        }
        
        // Remove stored caps not persistent on death
        Map<String, IInactiveCapabilities.Cap> caps = inactives.getCapabilityMap();
        List<String> capKeys = new ArrayList<String>();
        capKeys.addAll(caps.keySet());
        for (String key : capKeys) {
            IInactiveCapabilities.Cap cap = caps.get(key);
            if (!cap.persistsOnDeath) {
                caps.remove(key);
            }
        }
        
        if (ModState.isThaumcraftLoaded) {
            // Store away player's Thaumcraft knowledge and warp
            forgetThaumicKnowledgeAndWarp(player);
        }
    }
    
    /**
     * Prevent the player from crafting salis mundus if they haven't
     * had the dream yet.
     */
    @SubscribeEvent
    @Optional.Method(modid=ModState.THAUMCRAFT_ID)
    public void onCraftSalisMundus(EventTakeStack.Pre event) {
        ItemStack toCraft = event.slot.getStack();
        if (InventoryUtil.isEmptyItemStack(toCraft) ||
                toCraft.getItem() != ItemsTC.salisMundus ||
                !InventoryExtension.INSTANCE.isCraftingSlot(event.slot)) {
            return;
        }
        
        IPlayerKnowledge currentKnowledge = event.player.getCapability(THAUMCRAFT_KNOWLEDGE_CAPABILITY, null);
        if (currentKnowledge != null && !currentKnowledge.isResearchKnown("!gotdream")) {
            event.setCanceled(true);
        }
    }
    
    /**
     * Prevent the player form seeing the craft result for salis mundus
     * before the player is able to craft it.
     */
    @SubscribeEvent
    @Optional.Method(modid=ModState.THAUMCRAFT_ID)
    public void onSeeCraftSalisMundus(EventCraftPredict event) {
        if (!event.world.isRemote) {
            return;
        }
        
        ItemStack toCraft = event.craftResult;
        if (InventoryUtil.isEmptyItemStack(toCraft) || toCraft.getItem() != ItemsTC.salisMundus) {
            return;
        }
        
        EntityPlayer player = MiscVanilla.getTheMinecraftPlayer();
        IPlayerKnowledge currentKnowledge = player.getCapability(THAUMCRAFT_KNOWLEDGE_CAPABILITY, null);
        if (currentKnowledge != null && !currentKnowledge.isResearchKnown("!gotdream")) {
            event.setCanceled(true);
        }
    }
    
    /**
     * On crafting the Thauminomicon successfully, make player recall thaumic knowledge and warp from past life.
     * Also give player the research needed to start using the Thaumonomicon, since the research is no longer
     * obtained from simply picking up the book.
     */
    @SubscribeEvent
    @Optional.Method(modid=ModState.THAUMCRAFT_ID)
    public void onPlayerCreateThauminomicon(PlayerEvent.ItemCraftedEvent event) {
        if (event.crafting.getItem() != ItemsTC.thaumonomicon) {
            return;
        }
        
        recallThaumcraftIfNeeded(event.player);
        IPlayerKnowledge currentKnowledge = event.player.getCapability(THAUMCRAFT_KNOWLEDGE_CAPABILITY, null);
        if (currentKnowledge != null && !currentKnowledge.isResearchKnown("!gotthaumonomicon")) {
            currentKnowledge.addResearch("!gotthaumonomicon");
            // This is where Thaumcraft would sync player research, however there is no need since this runs on both sides.
        }
    }

    @Optional.Method(modid=ModState.THAUMCRAFT_ID)
    public static void recallThaumcraftIfNeeded(EntityPlayer player) {
        IInactiveCapabilities inactives = player.getCapability(INACTIVE_CAPABILITIES, null);
        if (inactives == null) {
            return;
        }
        
        if (!inactives.getCapabilityMap().containsKey(PAST_LIFE_THAUMIC_KNOWLEDGE) &&
                !inactives.getCapabilityMap().containsKey(PAST_LIFE_WARP)) {
            return;
        }
        
        recallThaumicKnowledgeAndWarp(player);
        if (player.world.isRemote && MiscVanilla.isTheMinecraftPlayer(player)) {
            Chat.messageSP(Chat.Type.THAUMIC, player, new TextComponentTranslation("hardcorealchemy.magic.recall_thaumcraft"));
            player.playSound(SoundsTC.whispers, 0.5F, 1.0F);
        }
    }
    
    @SubscribeEvent
    public void onChangeMagicState(PlayerTickEvent event) {
        if (event.phase != Phase.END) {
            return;
        }
        
        EntityPlayer player = event.player;
        
        IInactiveCapabilities inactives = player.getCapability(INACTIVE_CAPABILITIES, null);
        if (inactives == null) {
            return;
        }
        
        if (MorphExtension.INSTANCE.canUseHighMagic(player)) {
            if (ModState.isArsMagicaLoaded) {
                activateSpellcasting(player);
            }
            if (ModState.isThaumcraftLoaded) {
                activateThaumicKnowledgeAndWarp(player);
            }
        }
        else {
            if (ModState.isArsMagicaLoaded) {
                deactivateSpellcasting(player);
            }
            if (ModState.isThaumcraftLoaded) {
                deactivateThaumicKnowledgeAndWarp(player);
            }
        }
    }
    
    public static final String INACTIVE_THAUMIC_KNOWLEDGE = HardcoreAlchemyCore.MOD_ID + ":inactive_thaumic_knowledge";
    public static final String INACTIVE_WARP = HardcoreAlchemyCore.MOD_ID + ":inactive_warp";
    public static final String PAST_LIFE_THAUMIC_KNOWLEDGE = HardcoreAlchemyCore.MOD_ID + ":past_life_thaumic_knowledge";
    public static final String PAST_LIFE_WARP = HardcoreAlchemyCore.MOD_ID + ":past_life_warp";
    public static final String INACTIVE_MANA_POOL = HardcoreAlchemyCore.MOD_ID + ":mana_pool";
    
    /**
     * Re-applies Thaumcraft research progress and warp that has been stored
     * due to the player's past inability to use high magic
     * 
     * Potentially called every tick
     */
    @Optional.Method(modid=ModState.THAUMCRAFT_ID)
    public static void activateThaumicKnowledgeAndWarp(EntityPlayer player) {
        IInactiveCapabilities inactives = player.getCapability(INACTIVE_CAPABILITIES, null);
        if (inactives == null) {
            return;
        }

        IPlayerKnowledge currentKnowledge = player.getCapability(THAUMCRAFT_KNOWLEDGE_CAPABILITY, null);
        if (currentKnowledge != null) {
            IPlayerKnowledge inactiveKnowledge = fromCapMap(THAUMCRAFT_KNOWLEDGE_CAPABILITY, inactives, INACTIVE_THAUMIC_KNOWLEDGE);
            if (inactiveKnowledge != null) {
                // Knowledge needs activating
                mergeKnowledgeLeft(currentKnowledge, inactiveKnowledge);
                toCapMap(THAUMCRAFT_KNOWLEDGE_CAPABILITY, inactives, INACTIVE_THAUMIC_KNOWLEDGE, null, true);
                
            }
        }

        IPlayerWarp currentWarp = player.getCapability(WARP_CAPABILITY, null);
        if (currentWarp != null) {
            IPlayerWarp inactiveWarp = fromCapMap(WARP_CAPABILITY, inactives, INACTIVE_WARP);
            if (inactiveWarp != null) {
                // Warp needs activating
                currentWarp.deserializeNBT(inactiveWarp.serializeNBT());
                toCapMap(WARP_CAPABILITY, inactives, INACTIVE_WARP, null, true);
            }
        }
    }
    
    /**
     * Stores away Thaumcraft research progress and warp
     * due to the player's new inability to use high magic
     * 
     * Potentially called every tick
     */
    @Optional.Method(modid=ModState.THAUMCRAFT_ID)
    public static void deactivateThaumicKnowledgeAndWarp(EntityPlayer player) {
        IInactiveCapabilities inactives = player.getCapability(INACTIVE_CAPABILITIES, null);
        if (inactives == null) {
            return;
        }
        
        IPlayerKnowledge currentKnowledge = player.getCapability(THAUMCRAFT_KNOWLEDGE_CAPABILITY, null);
        if (currentKnowledge != null) {
            IPlayerKnowledge inactiveKnowledge = fromCapMap(THAUMCRAFT_KNOWLEDGE_CAPABILITY, inactives, INACTIVE_THAUMIC_KNOWLEDGE);
            if (inactiveKnowledge == null) {
                // Knowledge needs deactivating
                inactiveKnowledge = THAUMCRAFT_KNOWLEDGE_CAPABILITY.getDefaultInstance();
                mergeKnowledgeLeft(inactiveKnowledge, currentKnowledge);
                toCapMap(THAUMCRAFT_KNOWLEDGE_CAPABILITY, inactives, INACTIVE_THAUMIC_KNOWLEDGE, inactiveKnowledge, true);
                currentKnowledge.deserializeNBT(getPersistentThaumcraftKnowledge(inactiveKnowledge).serializeNBT());
            }
        }
        
        IPlayerWarp currentWarp = player.getCapability(WARP_CAPABILITY, null);
        if (currentWarp != null) {
            IPlayerWarp inactiveWarp = fromCapMap(WARP_CAPABILITY, inactives, INACTIVE_WARP);
            if (inactiveWarp == null) {
                // Warp needs deactivating
                inactiveWarp = WARP_CAPABILITY.getDefaultInstance();
                inactiveWarp.deserializeNBT(currentWarp.serializeNBT());
                toCapMap(WARP_CAPABILITY, inactives, INACTIVE_WARP, inactiveWarp, true);
                currentWarp.deserializeNBT(WARP_CAPABILITY.getDefaultInstance().serializeNBT());
            }
        }
    }
    
    /**
     * Causes player to temporarily forget their Thaumcraft knowledge
     * and warp due to death. Newly forgotten knowledge is combined with
     * existing forgotten knowledge. Warp is stored only if a player
     * has used Thaumcraft in this life, and overrides the previous value.
     */
    @Optional.Method(modid=ModState.THAUMCRAFT_ID)
    public static void forgetThaumicKnowledgeAndWarp(EntityPlayer player) {
        IInactiveCapabilities inactives = player.getCapability(INACTIVE_CAPABILITIES, null);
        if (inactives == null) {
            return;
        }
        
        IPlayerKnowledge currentKnowledge = player.getCapability(THAUMCRAFT_KNOWLEDGE_CAPABILITY, null);
        if (currentKnowledge != null) {
            IPlayerKnowledge inactiveKnowledge = fromCapMap(THAUMCRAFT_KNOWLEDGE_CAPABILITY, inactives, INACTIVE_THAUMIC_KNOWLEDGE);
            IPlayerKnowledge pastLifeKnowledge = fromCapMap(THAUMCRAFT_KNOWLEDGE_CAPABILITY, inactives, PAST_LIFE_THAUMIC_KNOWLEDGE);

            // Merge knowledge
            if (pastLifeKnowledge == null) {
                pastLifeKnowledge = THAUMCRAFT_KNOWLEDGE_CAPABILITY.getDefaultInstance();
            }
            mergeKnowledgeLeft(pastLifeKnowledge, currentKnowledge, inactiveKnowledge);
            // Store merged knowledge
            toCapMap(THAUMCRAFT_KNOWLEDGE_CAPABILITY, inactives, PAST_LIFE_THAUMIC_KNOWLEDGE, pastLifeKnowledge, true);

            // Reset current knowledge
            IPlayerKnowledge persistentKnowledge = getPersistentThaumcraftKnowledge(pastLifeKnowledge);
            if (inactiveKnowledge != null) {
                inactiveKnowledge.deserializeNBT(persistentKnowledge.serializeNBT());
                toCapMap(THAUMCRAFT_KNOWLEDGE_CAPABILITY, inactives, INACTIVE_THAUMIC_KNOWLEDGE, inactiveKnowledge, true);
            }
            currentKnowledge.deserializeNBT(persistentKnowledge.serializeNBT());
        }
        
        IPlayerWarp currentWarp = player.getCapability(WARP_CAPABILITY, null);
        if (currentWarp != null) {
            IPlayerWarp inactiveWarp = fromCapMap(WARP_CAPABILITY, inactives, INACTIVE_WARP);
            IPlayerWarp pastLifeWarp = fromCapMap(WARP_CAPABILITY, inactives, PAST_LIFE_WARP);
            
            if (pastLifeWarp == null) {
                // Store warp
                pastLifeWarp = WARP_CAPABILITY.getDefaultInstance();
                if (inactiveWarp != null) {
                    pastLifeWarp.deserializeNBT(inactiveWarp.serializeNBT());
                }
                else {
                    pastLifeWarp.deserializeNBT(currentWarp.serializeNBT());
                }
                toCapMap(WARP_CAPABILITY, inactives, PAST_LIFE_WARP, pastLifeWarp, true);

                // Clear warp
                IPlayerWarp noWarp = WARP_CAPABILITY.getDefaultInstance();
                if (inactiveWarp != null) {
                    toCapMap(WARP_CAPABILITY, inactives, INACTIVE_WARP, noWarp, true);
                }
                currentWarp.deserializeNBT(noWarp.serializeNBT());
            }
        }
    }
    
    /**
     * Causes player to recall their Thaumcraft knowledge and warp
     * from the last spawn they used the mod. Forgotten knowledge
     * is combined with existing knowledge. Warp is overridden.
     */
    @Optional.Method(modid=ModState.THAUMCRAFT_ID)
    public static void recallThaumicKnowledgeAndWarp(EntityPlayer player) {
        IInactiveCapabilities inactives = player.getCapability(INACTIVE_CAPABILITIES, null);
        if (inactives == null) {
            return;
        }
        
        IPlayerKnowledge currentKnowledge = player.getCapability(THAUMCRAFT_KNOWLEDGE_CAPABILITY, null);
        if (currentKnowledge != null) {
            IPlayerKnowledge pastLifeKnowledge = fromCapMap(THAUMCRAFT_KNOWLEDGE_CAPABILITY, inactives, PAST_LIFE_THAUMIC_KNOWLEDGE);
            if (pastLifeKnowledge != null) {
                // Past life knowledge is available and has not been recalled yet
                IPlayerKnowledge inactiveKnowledge = fromCapMap(THAUMCRAFT_KNOWLEDGE_CAPABILITY, inactives, INACTIVE_THAUMIC_KNOWLEDGE);
                
                if (inactiveKnowledge != null) {
                    // Magic is inactive (not normally possible during gameplay)
                    mergeKnowledgeLeft(inactiveKnowledge, pastLifeKnowledge);
                    toCapMap(THAUMCRAFT_KNOWLEDGE_CAPABILITY, inactives, INACTIVE_THAUMIC_KNOWLEDGE, inactiveKnowledge, true);
                    mergeKnowledgeLeft(currentKnowledge, getPersistentThaumcraftKnowledge(inactiveKnowledge));
                }
                else {
                    // Magic is active
                    mergeKnowledgeLeft(currentKnowledge, pastLifeKnowledge);
                }
                // Clear past life knowledge since it has been applied
                toCapMap(THAUMCRAFT_KNOWLEDGE_CAPABILITY, inactives, PAST_LIFE_THAUMIC_KNOWLEDGE, null, true);
            }
        }
        
        IPlayerWarp currentWarp = player.getCapability(WARP_CAPABILITY, null);
        if (currentWarp != null) {
            IPlayerWarp pastLifeWarp = fromCapMap(WARP_CAPABILITY, inactives, PAST_LIFE_WARP);
            if (pastLifeWarp != null) {
                IPlayerWarp inactiveWarp = fromCapMap(WARP_CAPABILITY, inactives, INACTIVE_WARP);
                if (inactiveWarp != null) {
                    // Magic is inactive (not normally possible during gameplay)
                    inactiveWarp.deserializeNBT(pastLifeWarp.serializeNBT());
                    toCapMap(WARP_CAPABILITY, inactives, INACTIVE_WARP, inactiveWarp, true);
                }
                else {
                    // Magic is active
                    currentWarp.deserializeNBT(pastLifeWarp.serializeNBT());
                }
                toCapMap(WARP_CAPABILITY, inactives, PAST_LIFE_WARP, null, true);
            }
            
        }
    }
    
    public static <T> T fromCapMap(Capability<T> capability, IInactiveCapabilities inactives, String key) {
        IInactiveCapabilities.Cap cap = inactives.getCapabilityMap().get(key);
        if (cap == null) {
            return null;
        }
        
        T t = capability.getDefaultInstance();
        capability.readNBT(t, null, cap.data);
        return t;
    }
    
    public static <T> void toCapMap(Capability<T> capability, IInactiveCapabilities inactives, String key,
            T value, boolean persistsOnDeath) {
        if (value == null) {
            inactives.getCapabilityMap().remove(key);
            return;
        }
        
        IInactiveCapabilities.Cap cap = inactives.getCapabilityMap().get(key);
        if (cap == null) {
            cap = new IInactiveCapabilities.Cap();
            inactives.getCapabilityMap().put(key, cap);
        }

        cap.persistsOnDeath = persistsOnDeath;
        cap.data = (NBTTagCompound)capability.writeNBT(value, null);
    }
    
    @Optional.Method(modid=ModState.THAUMCRAFT_ID)
    public static void mergeKnowledgeLeft(@Nullable IPlayerKnowledge knowledgeDest, @Nullable IPlayerKnowledge...knowledgeSources) {
        if (knowledgeDest == null) {
            return;
        }
        
        int nonNullKnowledges = 1;
        for (IPlayerKnowledge knowledgeSource : knowledgeSources) {
            if (knowledgeSource != null) {
                nonNullKnowledges++;
            }
        }
        
        IPlayerKnowledge[] nonNullKnowledgeSources = new IPlayerKnowledge[nonNullKnowledges];
        int i = 0;
        nonNullKnowledgeSources[i++] = knowledgeDest;
        for (IPlayerKnowledge knowledgeSource : knowledgeSources) {
            if (knowledgeSource != null) {
                nonNullKnowledgeSources[i++] = knowledgeSource;
            }
        }
        IPlayerKnowledge mergedKnowledge = mergeKnowledge(nonNullKnowledgeSources);
        
        knowledgeDest.deserializeNBT(mergedKnowledge.serializeNBT());
    }
    
    /**
     * Combines player knowledge. Research is combined based
     * on the most advanced stage, while knowledge categories
     * are combined cumulatively.
     */
    @Optional.Method(modid=ModState.THAUMCRAFT_ID)
    public static @Nonnull IPlayerKnowledge mergeKnowledge(@Nonnull IPlayerKnowledge... knowledgeSources) {
        IPlayerKnowledge mergedKnowledge = THAUMCRAFT_KNOWLEDGE_CAPABILITY.getDefaultInstance();
        
        for (IPlayerKnowledge knowledgeSource : knowledgeSources) {
            for (String researchKey : knowledgeSource.getResearchList()) {
                mergedKnowledge.addResearch(researchKey);
                mergedKnowledge.setResearchStage(researchKey,
                        Math.max(mergedKnowledge.getResearchStage(researchKey),
                                knowledgeSource.getResearchStage(researchKey))
                        );
            }
            
            for (EnumKnowledgeType knowledgeType : EnumKnowledgeType.values()) {
                for (ResearchCategory researchCategory : ResearchCategories.researchCategories.values()) {
                    int knowledgeAmount = knowledgeSource.getKnowledgeRaw(knowledgeType, researchCategory);
                    mergedKnowledge.addKnowledge(knowledgeType, researchCategory, knowledgeAmount);
                }
            }
            
        }
        
        return mergedKnowledge;
    }
    
    public static final Set<String> UNGATED_TC_RESEARCH =
            Sets.newHashSet(
                    "f_toomuchflux", "m_hellandback", "m_endoftheworld",
                    "m_walker", "m_runner", "m_jumper", "m_swimmer"
                    );
    
    @Optional.Method(modid=ModState.THAUMCRAFT_ID)
    public static void reduceToPersistentThaumcraftKnowledge(@Nullable IPlayerKnowledge knowledge) {
        if (knowledge == null) {
            return;
        }
        
        IPlayerKnowledge persistentKnowledge = getPersistentThaumcraftKnowledge(knowledge);
        knowledge.deserializeNBT(persistentKnowledge.serializeNBT());
    }
    
    /**
     * Gets the subset of player knowledge which is retained after
     * death/when the player cannot use high magic. This is to avoid
     * small annoyances like a player getting yet another chat message
     * that they have visited the Nether.
     */
    @Optional.Method(modid=ModState.THAUMCRAFT_ID)
    public static @Nonnull IPlayerKnowledge getPersistentThaumcraftKnowledge(@Nonnull IPlayerKnowledge knowledge) {
        IPlayerKnowledge persistentKnowledge = THAUMCRAFT_KNOWLEDGE_CAPABILITY.getDefaultInstance();
        
        for (String key : knowledge.getResearchList()) {
            if (UNGATED_TC_RESEARCH.contains(key)) {
                persistentKnowledge.addResearch(key);
                persistentKnowledge.setResearchStage(key, knowledge.getResearchStage(key));
            }
        }
        
        return persistentKnowledge;
    }
    
    /**
     * Whether the player can receive the messages required to start
     * Thaumcraft (ie the research for finding a vis crystal and
     * having a dream about it). Prevents player from "rediscovering"
     * Thaumcraft when they are just in a permanent morph that can't use
     * high magic right now.
     */
    @Optional.Method(modid=ModState.THAUMCRAFT_ID)
    @CoremodHook
    public static boolean canStartThaumcraftResearch(EntityPlayer player) {
        return MorphExtension.INSTANCE.canUseHighMagic(player);
    }
    
    /**
     * Whether the player should get the research needed to unlock the
     * first research entry simply by picking up a Thaumonomicon from the
     * ground.
     * Currently always false. Instead, the player must always craft the
     * Thaumonomicon themself.
     */
    @Optional.Method(modid=ModState.THAUMCRAFT_ID)
    @CoremodHook
    public static boolean canThaumonomiconPickupUnlockResearch(EntityPlayer player) {
        return false;
    }
    
    public static final String INACTIVE_AFFINITIES = HardcoreAlchemyCore.MOD_ID + ":inactive_affinities";
    
    /**
     * Re-applies Ars Magica affinity state and level that has been stored
     * due to the player's past inability to use high magic.
     */
    @Optional.Method(modid=ModState.ARS_MAGICA_ID)
    public static void activateSpellcasting(EntityPlayer player) {
        IAffinityData affinities = player.getCapability(AFFINITY_CAPABILITY, null);
        if (affinities == null) {
            return;
        }
        
        IInactiveCapabilities inactives = player.getCapability(INACTIVE_CAPABILITIES, null);
        if (inactives == null) {
            return;
        }
        
        ConcurrentMap<String, IInactiveCapabilities.Cap> caps = inactives.getCapabilityMap();
        IInactiveCapabilities.Cap affinityCap = caps.get(INACTIVE_AFFINITIES);
        if (affinityCap != null) {
            /* The affinity capability has been stored. Retrieve its
             * value and store it in the player's current capability.
             */
            AFFINITY_CAPABILITY.getStorage().readNBT(AFFINITY_CAPABILITY, affinities, null, affinityCap.data);
            
            caps.remove(INACTIVE_AFFINITIES);
        }
        
        IInactiveCapabilities.Cap manaPoolCap = caps.get(INACTIVE_MANA_POOL);
        if (manaPoolCap != null) {
            EntityExtension.For(player).deserializeNBT(manaPoolCap.data);
            caps.remove(INACTIVE_MANA_POOL);
        }
    }
    
    /**
     * Stores away Ars Magica affinity state and level
     * due to the player's new inability to use high magic.
     */
    @Optional.Method(modid=ModState.ARS_MAGICA_ID)
    public static void deactivateSpellcasting(EntityPlayer player) {
        IInactiveCapabilities inactives = player.getCapability(INACTIVE_CAPABILITIES, null);
        if (inactives == null) {
            return;
        }
        
        ConcurrentMap<String, IInactiveCapabilities.Cap> caps = inactives.getCapabilityMap();

        IInactiveCapabilities.Cap affinityCap = caps.get(INACTIVE_AFFINITIES);
        if (affinityCap == null) {
            IAffinityData affinities = player.getCapability(AFFINITY_CAPABILITY, null);
            if (affinities != null) {
                /* The affinity capability is not deactivated yet.
                 * Store the existing Ars Magica affinity capability here.
                 */
                affinityCap = new IInactiveCapabilities.Cap();
                affinityCap.data = (NBTTagCompound)(AFFINITY_CAPABILITY.getStorage().writeNBT(AFFINITY_CAPABILITY, affinities, null));
                affinityCap.persistsOnDeath = false;
                caps.put(INACTIVE_AFFINITIES, affinityCap);
                
                // Set all affinity depths to zero to prevent magical effects
                for (Affinity affinity : affinities.getAffinities().keySet()) {
                    affinities.setAffinityDepth(affinity, 0.0D);
                }
            }
        }

        IInactiveCapabilities.Cap manaPoolCap = caps.get(INACTIVE_MANA_POOL);
        if (manaPoolCap == null) {
            EntityExtension extension = EntityExtension.For(player);
            if (extension != null) {
                manaPoolCap = new IInactiveCapabilities.Cap();
                manaPoolCap.data = (NBTTagCompound)extension.serializeNBT();
                manaPoolCap.persistsOnDeath = false;
                caps.put(INACTIVE_MANA_POOL, manaPoolCap);
                
                extension.setMagicLevelWithMana(1);
                extension.setCurrentXP(0.0F);
            }
        }
    }
}
