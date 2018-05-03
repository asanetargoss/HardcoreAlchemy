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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import WayofTime.bloodmagic.api.saving.SoulNetwork;
import WayofTime.bloodmagic.api.util.helper.NetworkHelper;
import am2.api.affinity.Affinity;
import am2.api.extensions.IAffinityData;
import am2.api.extensions.IEntityExtension;
import am2.api.extensions.ISkillData;
import am2.api.skill.Skill;
import am2.api.skill.SkillPoint;
import am2.extensions.AffinityData;
import am2.extensions.EntityExtension;
import am2.extensions.SkillData;
import moze_intel.projecte.api.ProjectEAPI;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.registry.GameRegistry;
import targoss.hardcorealchemy.ModState;
import targoss.hardcorealchemy.capability.humanity.ICapabilityHumanity;
import targoss.hardcorealchemy.config.Configs;
import targoss.hardcorealchemy.coremod.CoremodHook;
import targoss.hardcorealchemy.coremod.ObfuscatedName;
import targoss.hardcorealchemy.event.EventTakeStack;
import targoss.hardcorealchemy.util.Chat;
import targoss.hardcorealchemy.util.Interaction;
import targoss.hardcorealchemy.util.InvokeUtil;
import targoss.hardcorealchemy.util.MorphState;

public class ListenerPlayerMagic extends ConfiguredListener {
    public ListenerPlayerMagic(Configs configs) {
        super(configs);
    }
    
    /**
     *  Mods of a certain type of magic are forbidden for use by [most] permanent morphs.
     *  Generally these forms of magic are powerful, and human-like due to rigorous
     *  requirements of research and study.
     */
    public static final Set<String> HIGH_MAGIC_MODS; 
    public static final Set<String> MAGIC_ITEM_ALLOW_USE;
    public static final Set<String> MAGIC_ITEM_ALLOW_CRAFT;
    public static final Set<String> MAGIC_BLOCK_ALLOW_USE;
    
    @CapabilityInject(ICapabilityHumanity.class)
    public static final Capability<ICapabilityHumanity> HUMANITY_CAPABILITY = null;
    
    static {
        /* Note: There is no need to add ItemBlocks, ItemFoods,
         * or blocks and items without right click uses to
         * these whitelists. They will be detected automatically.
         */
        
        HIGH_MAGIC_MODS = new HashSet<String>();
        HIGH_MAGIC_MODS.add(ModState.ARS_MAGICA_ID);
        // Yes, ProjectE is here with two different spellings. That's intentional.
        HIGH_MAGIC_MODS.add(ModState.PROJECT_E_ID);
        HIGH_MAGIC_MODS.add("projecte");
        HIGH_MAGIC_MODS.add("astralsorcery");
        HIGH_MAGIC_MODS.add(ModState.THAUMCRAFT_ID);
        
        MAGIC_ITEM_ALLOW_USE = new HashSet<String>();
        MAGIC_ITEM_ALLOW_USE.add("projecte:item.pe_alchemical_bag");
        MAGIC_ITEM_ALLOW_USE.add("arsmagica2:workbench_upgrade");
        /* TODO: Add custom handler to prevent chalk from being used
         * and remove it from the whitelist
         */
        MAGIC_ITEM_ALLOW_USE.add("arsmagica2:chalk");
        
        MAGIC_ITEM_ALLOW_CRAFT = new HashSet<String>();
        MAGIC_ITEM_ALLOW_CRAFT.add("projecte:item.pe_covalence_dust");
        MAGIC_ITEM_ALLOW_CRAFT.add("projecte:alchemical_chest");
        MAGIC_ITEM_ALLOW_CRAFT.add("arsmagica2:magicians_workbench");
        MAGIC_ITEM_ALLOW_CRAFT.add("arsmagica2:workbench_upgrade");
        
        MAGIC_BLOCK_ALLOW_USE = new HashSet<String>();
        MAGIC_BLOCK_ALLOW_USE.add("projecte:alchemical_chest");
        MAGIC_BLOCK_ALLOW_USE.add("arsmagica2:magicians_workbench");
    }
    
    /*TODO: Prevent using block transmutation feature of Philosopher Stone
     */
    
    @SubscribeEvent
    public void onPlayerTickMP(TickEvent.PlayerTickEvent event) {
        if (event.phase != Phase.END) {
            return;
        }
        EntityPlayer player = event.player;
        ICapabilityHumanity capabilityHumanity = player.getCapability(HUMANITY_CAPABILITY, null);
        if (capabilityHumanity != null) {
            capabilityHumanity.setNotifiedMagicFail(false);
        }
    }
    
    @SubscribeEvent
    public void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        EntityPlayer player = event.getEntityPlayer();
        ICapabilityHumanity capabilityHumanity = player.getCapability(HUMANITY_CAPABILITY, null);
        if (capabilityHumanity == null || MorphState.canUseHighMagic(player)) {
            return;
        }
        if (!isAllowed(MAGIC_ITEM_ALLOW_USE, event.getItemStack())) {
            event.setCanceled(true);
            if (!capabilityHumanity.getNotifiedMagicFail()) {
                capabilityHumanity.setNotifiedMagicFail(true);
                if (player.world.isRemote) {
                    Chat.notifySP(player, new TextComponentTranslation("hardcorealchemy.magic.disabled.item"));
                }
            }
        }
    }
    
    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Block block = event.getWorld().getBlockState(event.getPos()).getBlock();
        EntityPlayer player = event.getEntityPlayer();
        ICapabilityHumanity capabilityHumanity = player.getCapability(HUMANITY_CAPABILITY, null);
        if (capabilityHumanity == null || MorphState.canUseHighMagic(player)) {
            return;
        }
        if (!isAllowed(MAGIC_BLOCK_ALLOW_USE, block)) {
            event.setUseBlock(Result.DENY);
            if (!capabilityHumanity.getNotifiedMagicFail()) {
                capabilityHumanity.setNotifiedMagicFail(true);
                if (player.world.isRemote) {
                    Chat.notifySP(player, new TextComponentTranslation("hardcorealchemy.magic.disabled.block"));
                }
            }
        }
    }
    
    @SubscribeEvent
    public void onTakeStackPre(EventTakeStack.Pre event) {
        if (event.isCanceled()) {
            return;
        }
        ItemStack craftResult = event.slot.getStack();
        if (craftResult == null || !(event.slot instanceof SlotCrafting)) {
            return;
        }
        
        EntityPlayer player = event.player;
        ICapabilityHumanity capabilityHumanity = player.getCapability(HUMANITY_CAPABILITY, null);
        if (capabilityHumanity != null &&
                !MorphState.canUseHighMagic(player) &&
                !isAllowed(MAGIC_ITEM_ALLOW_CRAFT, craftResult)) {
            event.setCanceled(true);
            if (!capabilityHumanity.getNotifiedMagicFail()) {
                capabilityHumanity.setNotifiedMagicFail(true);
                if (player.world.isRemote) {
                    Chat.notifySP(player, new TextComponentTranslation("hardcorealchemy.magic.disabled.craft"));
                }
            }
        }
    }
    
    @CoremodHook
    public static boolean canUseProjectEKeybinds(EntityPlayerMP player) {
        ICapabilityHumanity capabilityHumanity = player.getCapability(HUMANITY_CAPABILITY, null);
        if (capabilityHumanity == null || MorphState.canUseHighMagic(player)) {
            return true;
        }
        if (!capabilityHumanity.getNotifiedMagicFail()) {
            capabilityHumanity.setNotifiedMagicFail(true);
            Chat.notify(player, new TextComponentTranslation("hardcorealchemy.magic.disabled.projectekeypress"));
        }
        return false;
    }
    
    @SubscribeEvent
    public void onMageDie(PlayerRespawnEvent event) {
        eraseAllMortalMagic(event.player);
    }
    
    public static boolean isAllowed(Set<String> whitelist, ItemStack itemStack) {
        Item item = itemStack.getItem();
        
        if (!Interaction.hasSpecialUse(item)) {
            return true;
        }
        
        ResourceLocation itemResource = item.getRegistryName();
        return !HIGH_MAGIC_MODS.contains(itemResource.getResourceDomain()) ||
                    whitelist.contains(itemResource.toString());
    }
    
    public static boolean isAllowed(Set<String> whitelist, Block block) {
        if (!Interaction.hasSpecialUse(block)) {
            return true;
        }
        
        ResourceLocation blockResource = block.getRegistryName();
        return !HIGH_MAGIC_MODS.contains(blockResource.getResourceDomain()) ||
                    whitelist.contains(blockResource.toString());
    }
    
    public static void eraseAllMortalMagic(EntityPlayer player) {
        if (ModState.isBloodMagicLoaded) {
            eraseBloodMagic(player);
        }
        if (ModState.isArsMagicaLoaded) {
            eraseSpellMagic(player);
        }
        if (ModState.isProjectELoaded) {
            eraseEMC(player);
        }
        //TODO: Clear Astral Sorcery passive buffs?
    }
    
    /*
     * What it does: Makes it as if the player never used a blood orb.
     * Why it does it: 1) Prevents syphoning effect from Life Drain mod
     * 2) Hopefully stops rituals from running, but I haven't tested that so who knows?
     * 3) For the challenge
     */
    @Optional.Method(modid = ModState.BLOOD_MAGIC_ID)
    public static void eraseBloodMagic(EntityPlayer player) {
        SoulNetwork network = NetworkHelper.getSoulNetwork(player);
        network.setOrbTier(0);
        network.setCurrentEssence(0);
    }
    
    /*
     * What it does: Resets magic level, skill allocations, and affinities
     * Why it does it: 1) Prevents affinity effects from persisting across lives
     * 2) Sets other aspects of the player's magic to be consistent with that fact
     * 3) Allow for the player to try new specializations
     */
    @Optional.Method(modid = ModState.ARS_MAGICA_ID)
    public static void eraseSpellMagic(EntityPlayer player) {
        
        IEntityExtension playerMagicExtension = EntityExtension.For(player);
        if (playerMagicExtension != null) {
            playerMagicExtension.setMagicLevelWithMana(1);
            playerMagicExtension.setCurrentXP(0.0F);
            
        }
        
        ISkillData playerSkillData = SkillData.For(player);
        if (playerSkillData != null) {
            Map<SkillPoint, Integer> playerSkillPoints = playerSkillData.getSkillPoints();
            for (SkillPoint skillPoint : playerSkillPoints.keySet()) {
                if (skillPoint == SkillPoint.SKILL_POINT_1 /*blue skill point*/) {
                    playerSkillPoints.replace(skillPoint, 3);
                }
                else {
                    playerSkillPoints.replace(skillPoint, 0);
                }
            }
            Map<Skill, Boolean> playerSkills = playerSkillData.getSkills();
            for (Skill skill : playerSkills.keySet()) {
                playerSkills.replace(skill, false);
            }
        }
        
        IAffinityData playerAffinityData = AffinityData.For(player);
        if (playerAffinityData != null) {
            playerAffinityData.setLocked(false);
            for (Affinity affinity : GameRegistry.findRegistry(Affinity.class).getValues()) {
                playerAffinityData.setAffinityDepth(affinity, 0.0D);
            }
        }
    }
    
    /*
     * What it does: Sets EMC for the player's transmutation table to zero
     * Why it does it: 1) To prevent the player from transferring ridiculous amounts
     * of EMC across deaths
     * 2) Gently encourage players to not use the transmutation table for long-term storage
     */
    @Optional.Method(modid = ModState.PROJECT_E_ID)
    public static void eraseEMC(EntityPlayer player) {
        IKnowledgeProvider transmutationKnowledge = player.getCapability(ProjectEAPI.KNOWLEDGE_CAPABILITY, null);
        if (transmutationKnowledge != null) {
            transmutationKnowledge.setEmc(0.0D);
        }
    }
}
