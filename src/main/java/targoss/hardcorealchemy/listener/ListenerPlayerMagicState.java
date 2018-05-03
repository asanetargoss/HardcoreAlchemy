/*
 * Copyright 2018 asanetargoss
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
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.Clone;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.ModState;
import targoss.hardcorealchemy.capability.CapUtil;
import targoss.hardcorealchemy.capability.inactive.IInactiveCapabilities;
import targoss.hardcorealchemy.capability.inactive.InactiveCapabilities;
import targoss.hardcorealchemy.capability.inactive.ProviderInactiveCapabilities;
import targoss.hardcorealchemy.config.Configs;
import targoss.hardcorealchemy.coremod.CoremodHook;
import targoss.hardcorealchemy.util.Chat;
import targoss.hardcorealchemy.util.MorphState;
import thaumcraft.api.capabilities.IPlayerKnowledge;
import thaumcraft.api.capabilities.IPlayerKnowledge.EnumKnowledgeType;
import thaumcraft.api.items.ItemsTC;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchCategory;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.api.capabilities.IPlayerWarp;

/**
 * Handles capabilities from other magic mods, ex: make Ars Magica
 * affinities only active when the player is capable of using high magic.
 */
public class ListenerPlayerMagicState extends ConfiguredListener {
    public ListenerPlayerMagicState(Configs configs) {
        super(configs);
    }
    
    @CapabilityInject(IInactiveCapabilities.class)
    private static final Capability<IInactiveCapabilities> INACTIVE_CAPABILITIES = null;
    private static final ResourceLocation INACTIVE_CAPABILITIES_RESOURCE = InactiveCapabilities.RESOURCE_LOCATION;
    
    @CapabilityInject(IAffinityData.class)
    private static final Capability<IAffinityData> AFFINITY_CAPABILITY = null;
    
    @CapabilityInject(IPlayerKnowledge.class)
    private static final Capability<IPlayerKnowledge> THAUMCRAFT_KNOWLEDGE_CAPABILITY = null;
    
    @CapabilityInject(IPlayerWarp.class)
    private static final Capability<IPlayerWarp> WARP_CAPABILITY = null;
    
    @SubscribeEvent
    public void onAttachCapability(AttachCapabilitiesEvent<Entity> event) {
        Entity entity = event.getObject();
        if (entity instanceof EntityPlayer) {
            event.addCapability(INACTIVE_CAPABILITIES_RESOURCE, new ProviderInactiveCapabilities());
        }
    }
    
    @SubscribeEvent
    public void onPlayerClone(Clone event) {
        // Clone the capability container itself
        EntityPlayer player = event.getEntityPlayer();
        EntityPlayer playerOld = event.getOriginal();
        CapUtil.copyOldToNew(INACTIVE_CAPABILITIES, playerOld, player);
        // Note: Pruning of individual inactive capabilities only occurs on respawn
    }
    
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
        
        // Store away player's thaumcraft knowledge and warp
        forgetThaumicKnowledgeAndWarp(player);
    }
    
    /**
     * On using item, make player recall thaumic knowledge and warp from past life
     * (but only if the player can use high magic)
     * 
     * We assume that by the time this event listener is called,
     * all event canceling has already occurred, due to the low priority.
     */
    @SubscribeEvent(priority=EventPriority.LOWEST)
    @Optional.Method(modid=ModState.THAUMCRAFT_ID)
    public void onPlayerUseThaumicItem(PlayerInteractEvent.RightClickItem event) {
        // No need to check event.isCanceled()==true since that would mean this method is never called
        
        ResourceLocation itemResource = event.getItemStack().getItem().getRegistryName();
        if (!itemResource.getResourceDomain().equals(ModState.THAUMCRAFT_ID)) {
            return;
        }
        
        if (ListenerPlayerMagic.isAllowed(ListenerPlayerMagic.MAGIC_ITEM_ALLOW_USE, event.getItemStack()) ||
                event.getItemStack().getItem() == ItemsTC.salisMundus) {
            /* If the item is usable without high magic knowledge, there's
             * no reason it should trigger a memory flashback.
             * Also, this discludes salis mundus usage
             * as a way to recall Thaumcraft (makes opening the
             * Thauminomicon the dramatic moment)
             */
            return;
        }

        recallThaumcraftIfNeeded(event.getEntityPlayer());
    }
    
    /**
     * On using block, make player recall thaumic knowledge and warp from past life
     * (but only if the player can use high magic)
     * 
     * We assume that by the time this event listener is called,
     * all event canceling/denying has already occurred, due to the low priority.
     */
    @SubscribeEvent(priority=EventPriority.LOWEST)
    @Optional.Method(modid=ModState.THAUMCRAFT_ID)
    public void onPlayerUseThaumicBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getUseBlock() == Result.DENY) {
            return;
        }
        
        Block block = event.getWorld().getBlockState(event.getPos()).getBlock();
        if (block == null) {
            return;
        }
        
        ResourceLocation blockResource = block.getRegistryName();
        if (!blockResource.getResourceDomain().equals(ModState.THAUMCRAFT_ID)) {
            return;
        }
        
        if (ListenerPlayerMagic.isAllowed(ListenerPlayerMagic.MAGIC_BLOCK_ALLOW_USE, block)) {
            /* If the block is usable without high magic knowledge, there's
             * no reason it should trigger a memory flashback.
             */
            return;
        }

        recallThaumcraftIfNeeded(event.getEntityPlayer());
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
        if (player.world.isRemote && isTheMinecraftPlayer(player)) {
            Chat.notifyThaumicSP(player, new TextComponentTranslation("hardcorealchemy.magic.recall_thaumcraft"));
            player.playSound(SoundsTC.whispers, 0.5F, 1.0F);
        }
    }
    
    //TODO: Move to client-only utility class and have it more widely referenced
    @SideOnly(Side.CLIENT)
    public static boolean isTheMinecraftPlayer(EntityPlayer player) {
        return Minecraft.getMinecraft().player == player;
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
        
        if (MorphState.canUseHighMagic(player)) {
            if (ModState.isArsMagicaLoaded) {
                activateAffinities(player);
            }
            if (ModState.isThaumcraftLoaded) {
                activateThaumicKnowledgeAndWarp(player);
            }
        }
        else {
            if (ModState.isArsMagicaLoaded) {
                deactivateAffinities(player);
            }
            if (ModState.isThaumcraftLoaded) {
                deactivateThaumicKnowledgeAndWarp(player);
            }
        }
    }
    
    public static final String INACTIVE_THAUMIC_KNOWLEDGE = HardcoreAlchemy.MOD_ID + ":inactive_thaumic_knowledge";
    public static final String INACTIVE_WARP = HardcoreAlchemy.MOD_ID + ":inactive_warp";
    public static final String PAST_LIFE_THAUMIC_KNOWLEDGE = HardcoreAlchemy.MOD_ID + ":past_life_thaumic_knowledge";
    public static final String PAST_LIFE_WARP = HardcoreAlchemy.MOD_ID + ":past_life_warp";
    
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
                    int knowledgeAmount = knowledgeSource.getKnowledge(knowledgeType, researchCategory);
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
        return MorphState.canUseHighMagic(player);
    }
    
    public static final String INACTIVE_AFFINITIES = HardcoreAlchemy.MOD_ID + ":inactive_affinities";
    
    /**
     * Re-applies Ars Magica affinity state that has been stored
     * due to the player's past inability to use high magic
     */
    @Optional.Method(modid=ModState.ARS_MAGICA_ID)
    public static void activateAffinities(EntityPlayer player) {
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
    }
    
    /**
     * Stores away Ars Magica affinity state
     * due to the player's new inability to use high magic
     */
    @Optional.Method(modid=ModState.ARS_MAGICA_ID)
    public static void deactivateAffinities(EntityPlayer player) {
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
        if (affinityCap == null) {
            /* The affinity capability is not deactivated yet.
             * Store the existing Ars Magica affinity capability here.
             */
            affinityCap = new IInactiveCapabilities.Cap();
            affinityCap.data = (NBTTagCompound)(AFFINITY_CAPABILITY.getStorage().writeNBT(AFFINITY_CAPABILITY, affinities, null));
            affinityCap.persistsOnDeath = false;
            caps.put(INACTIVE_AFFINITIES, affinityCap);
        }
        
        // Set all affinity depths to zero to prevent magical effects
        for (Affinity affinity : affinities.getAffinities().keySet()) {
            affinities.setAffinityDepth(affinity, 0.0D);
        }
    }
}
