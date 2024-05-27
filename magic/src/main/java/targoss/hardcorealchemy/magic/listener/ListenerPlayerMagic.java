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

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Sets;

import WayofTime.bloodmagic.api.event.RitualEvent.RitualActivatedEvent;
import WayofTime.bloodmagic.api.event.RitualEvent.RitualRunEvent;
import WayofTime.bloodmagic.api.ritual.Ritual.BreakType;
import WayofTime.bloodmagic.api.saving.SoulNetwork;
import WayofTime.bloodmagic.api.util.helper.NetworkHelper;
import WayofTime.bloodmagic.tile.TileMasterRitualStone;
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
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.IForgeRegistryEntry;
import targoss.hardcorealchemy.ModState;
import targoss.hardcorealchemy.capability.humanity.ICapabilityHumanity;
import targoss.hardcorealchemy.capability.humanity.ProviderHumanity;
import targoss.hardcorealchemy.capability.misc.ICapabilityMisc;
import targoss.hardcorealchemy.capability.tilehistory.CapabilityTileHistory;
import targoss.hardcorealchemy.capability.tilehistory.ICapabilityTileHistory;
import targoss.hardcorealchemy.capability.tilehistory.ProviderTileHistory;
import targoss.hardcorealchemy.coremod.CoremodHook;
import targoss.hardcorealchemy.event.EventTakeStack;
import targoss.hardcorealchemy.listener.HardcoreAlchemyListener;
import targoss.hardcorealchemy.listener.ListenerPlayerResearch;
import targoss.hardcorealchemy.magic.event.EventRegenMana;
import targoss.hardcorealchemy.magic.research.Studies;
import targoss.hardcorealchemy.magic.will.WillState;
import targoss.hardcorealchemy.magic.will.Wills;
import targoss.hardcorealchemy.util.Chat;
import targoss.hardcorealchemy.util.Interaction;
import targoss.hardcorealchemy.util.InventoryExtension;
import targoss.hardcorealchemy.util.InventoryUtil;
import targoss.hardcorealchemy.util.MiscVanilla;
import targoss.hardcorealchemy.util.MorphExtension;

public class ListenerPlayerMagic extends HardcoreAlchemyListener {
    /**
     *  Mods of a certain type of magic are forbidden for use by [most] permanent morphs.
     *  Generally these forms of magic are powerful, and human-like due to rigorous
     *  requirements of research and study.
     */
    public static final Set<String> HIGH_MAGIC_MODS; 
    public static final Set<String> MAGIC_ITEM_ALLOW_USE;
    public static final Set<String> MAGIC_ITEM_ALLOW_CRAFT;
    public static final Set<String> MAGIC_BLOCK_ALLOW_USE;
    
    public static final Set<String> MAGIC_OBJECT_REQUIRES_FULL_MOON = Sets.newHashSet(
            "projecte:transmutation_table",
            "projecte:item.pe_transmutation_tablet"
            );
    
    @CapabilityInject(ICapabilityHumanity.class)
    public static final Capability<ICapabilityHumanity> HUMANITY_CAPABILITY = null;
    
    @CapabilityInject(ICapabilityTileHistory.class)
    public static final Capability<ICapabilityTileHistory> TILE_HISTORY_CAPABILITY = null;
    
    @CapabilityInject(ICapabilityMisc.class)
    public static final Capability<ICapabilityMisc> MISC_CAPABILITY = null;
    
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
        HIGH_MAGIC_MODS.add("runesofwizardry");
        HIGH_MAGIC_MODS.add("Psi");
        HIGH_MAGIC_MODS.add("psi");
        HIGH_MAGIC_MODS.add("wizardry");
        HIGH_MAGIC_MODS.add("notenoughwands");
        HIGH_MAGIC_MODS.add("betterbuilderswands");
        
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
        MAGIC_ITEM_ALLOW_CRAFT.add("arsmagica2:witchwood_sapling");
        MAGIC_ITEM_ALLOW_CRAFT.add("arsmagica2:witchwood_log");
        MAGIC_ITEM_ALLOW_CRAFT.add("arsmagica2:witchwood_leaves");
        MAGIC_ITEM_ALLOW_CRAFT.add("arsmagica2:witchwood_planks");
        MAGIC_ITEM_ALLOW_CRAFT.add("arsmagica2:witchwood_stairs");
        MAGIC_ITEM_ALLOW_CRAFT.add("arsmagica2:witchwood_slab");
        MAGIC_ITEM_ALLOW_CRAFT.add("thaumcraft:sapling");
        MAGIC_ITEM_ALLOW_CRAFT.add("thaumcraft:log");
        MAGIC_ITEM_ALLOW_CRAFT.add("thaumcraft:leaf");
        MAGIC_ITEM_ALLOW_CRAFT.add("thaumcraft:plank");
        MAGIC_ITEM_ALLOW_CRAFT.add("thaumcraft:greatwood_stairs");
        MAGIC_ITEM_ALLOW_CRAFT.add("thaumcraft:silverwood_stairs");
        MAGIC_ITEM_ALLOW_CRAFT.add("thaumcraft:slab_wood");
        
        MAGIC_BLOCK_ALLOW_USE = new HashSet<String>();
        MAGIC_BLOCK_ALLOW_USE.add("projecte:alchemical_chest");
        MAGIC_BLOCK_ALLOW_USE.add("arsmagica2:magicians_workbench");
    }

    /** Spell items that work differently outside the overworld */
    public static final Set<String> OVERWORLD_SPELL_ITEMS = new HashSet<>();
    
    static {
        OVERWORLD_SPELL_ITEMS.add("arsmagica2:spell_component");
        OVERWORLD_SPELL_ITEMS.add("arsmagica2:spell_staff_magitech");
        OVERWORLD_SPELL_ITEMS.add("arsmagica2:arcane_spellbook");
        OVERWORLD_SPELL_ITEMS.add("arsmagica2:spell");
        OVERWORLD_SPELL_ITEMS.add("arsmagica2:spell_book");
    }
    
    /*TODO: Prevent using block transmutation feature of Philosopher Stone
     */
    
    private static final String MAGIC_NOT_ALLOWED = "magic_not_allowed";

    public static boolean isCraftingAllowed(EntityPlayer player, ItemStack craftResult) {
        ICapabilityHumanity capabilityHumanity = player.getCapability(ProviderHumanity.HUMANITY_CAPABILITY, null);
        if (capabilityHumanity != null &&
                !MorphExtension.INSTANCE.canUseHighMagic(player) &&
                !isCraftingAllowedWhenMagicHindered(craftResult)) {
            return false;
        }
        
        return true;
    }

    public static boolean isCraftingAllowedWhenMagicHindered(ItemStack craftResult) {
        Item item = craftResult.getItem();
        
        ResourceLocation itemResource = item.getRegistryName();
        if (ListenerPlayerMagic.HIGH_MAGIC_MODS.contains(itemResource.getResourceDomain()) &&
                !ListenerPlayerMagic.MAGIC_ITEM_ALLOW_CRAFT.contains(itemResource.toString())) {
            return false;
        }
        
        return true;
    }
    
    @SubscribeEvent
    @Optional.Method(modid=ModState.BLOOD_MAGIC_ID)
    @SuppressWarnings("deprecation")
    public void onAttachTileCapability(AttachCapabilitiesEvent.TileEntity event) {
        TileEntity tileEntity = event.getObject();
        if (tileEntity instanceof TileMasterRitualStone) {
            event.addCapability(CapabilityTileHistory.RESOURCE_LOCATION, new ProviderTileHistory());
        }
    }
    
    protected float getManaRegenMultiplierAtEntity(EntityLivingBase entity) {
        BlockPos entityPos = new BlockPos(entity.posX, entity.posY, entity.posZ);
        float willAir = WillState.getWillAmount(Wills.AURA_AIR, entity.world, entityPos);
        return willAir;
    }
    
    @SubscribeEvent
    public void onRegenMana(EventRegenMana event) {
        float regenMultiplier = getManaRegenMultiplierAtEntity(event.entity);
        event.finalManaChange *= regenMultiplier;
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerCastSpell(PlayerInteractEvent.RightClickItem event) {
        EntityPlayer player = event.getEntityPlayer();
        ItemStack itemStack = event.getItemStack();
        if (InventoryUtil.isEmptyItemStack(itemStack)) {
            return;
        }
        if (!OVERWORLD_SPELL_ITEMS.contains(itemStack.getItem().getRegistryName().toString())) {
            return;
        }
        if (!WillState.isTypicalMagicCastingEnvironment(player.world, new BlockPos(player.posX, player.posY, player.posZ))) {
            ListenerPlayerResearch.acquireFactAndSendChatMessage(player, Studies.FACT_AURA_CASTING_WARNING);
        }
    }
    
    @SubscribeEvent
    public void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        EntityPlayer player = event.getEntityPlayer();
        ItemStack itemStack = event.getItemStack();
        ICapabilityHumanity capabilityHumanity = player.getCapability(HUMANITY_CAPABILITY, null);
        if (capabilityHumanity != null && !MorphExtension.INSTANCE.canUseHighMagic(player) && !isUseAllowed(itemStack)) {
            event.setCanceled(true);
            if (player.world.isRemote) {
                Chat.messageSP(Chat.Type.NOTIFY, player, new TextComponentTranslation("hardcorealchemy.magic.disabled.item"), 2, MAGIC_NOT_ALLOWED);
            }
            return;
        }
        if (itemStack != null && isHinderedByMoonPhase(player, itemStack.getItem())) {
            event.setCanceled(true);
            if (player.world.isRemote) {
                Chat.messageSP(Chat.Type.NOTIFY, player, new TextComponentTranslation("hardcorealchemy.magic.disabled.not_full_moon"), 2, MAGIC_NOT_ALLOWED);
            }
        }
    }
    
    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Block block = event.getWorld().getBlockState(event.getPos()).getBlock();
        EntityPlayer player = event.getEntityPlayer();
        ICapabilityHumanity capabilityHumanity = player.getCapability(HUMANITY_CAPABILITY, null);
        if (capabilityHumanity != null && !MorphExtension.INSTANCE.canUseHighMagic(player) && !isUseAllowed(block)) {
            event.setUseBlock(Result.DENY);
            if (player.world.isRemote) {
                Chat.messageSP(Chat.Type.NOTIFY, player, new TextComponentTranslation("hardcorealchemy.magic.disabled.block"), 2, MAGIC_NOT_ALLOWED);
            }
            return;
        }
        if (isHinderedByMoonPhase(player, block)) {
            event.setCanceled(true);
            if (player.world.isRemote) {
                Chat.messageSP(Chat.Type.NOTIFY, player, new TextComponentTranslation("hardcorealchemy.magic.disabled.not_full_moon"), 2, MAGIC_NOT_ALLOWED);
            }
        }
    }
    
    @SubscribeEvent
    public void onTakeStackPre(EventTakeStack.Pre event) {
        if (event.isCanceled()) {
            return;
        }
        ItemStack craftResult = event.slot.getStack();
        if (InventoryUtil.isEmptyItemStack(craftResult) || !InventoryExtension.INSTANCE.isCraftingSlot(event.slot)) {
            return;
        }
        
        EntityPlayer player = event.player;
        ICapabilityHumanity capabilityHumanity = player.getCapability(HUMANITY_CAPABILITY, null);
        if (capabilityHumanity != null &&
                !MorphExtension.INSTANCE.canUseHighMagic(player) &&
                !isCraftingAllowed(craftResult)) {
            event.setCanceled(true);
        }
    }
    
    @CoremodHook
    public static boolean canUseProjectEKeybinds(EntityPlayerMP player) {
        ICapabilityHumanity capabilityHumanity = player.getCapability(HUMANITY_CAPABILITY, null);
        if (capabilityHumanity == null || MorphExtension.INSTANCE.canUseHighMagic(player)) {
            return true;
        }
        Chat.message(Chat.Type.NOTIFY, player, new TextComponentTranslation("hardcorealchemy.magic.disabled.projectekeypress"), 2, MAGIC_NOT_ALLOWED);
        return false;
    }
    
    @SubscribeEvent
    public void onMageDie(PlayerRespawnEvent event) {
        eraseAllMortalMagic(event.player);
        if (ModState.isBloodMagicLoaded) {
            fixSoulNetworkCachingDeadPlayer(event.player);
        }
    }
    
    public static boolean requiresFullMoon(IForgeRegistryEntry.Impl<?> useTarget) {
        if (useTarget == null) {
            return false;
        }
        ResourceLocation resource = useTarget.getRegistryName();
        return resource != null && MAGIC_OBJECT_REQUIRES_FULL_MOON.contains(resource.toString());
    }
    
    public static boolean isHinderedByMoonPhase(EntityPlayer player, IForgeRegistryEntry.Impl<?> useTarget) {
        if (useTarget != null &&
                player.world.provider.getMoonPhase(player.world.getWorldTime()) != MiscVanilla.MoonPhase.FULL_MOON.ordinal()
                && requiresFullMoon(useTarget)) {
            return true;
        }
        return false;
    }
    
    public static boolean isUseAllowed(ItemStack itemStack) {
        Item item = itemStack.getItem();
        
        if (!Interaction.hasSpecialUse(item)) {
            return true;
        }
        
        ResourceLocation itemResource = item.getRegistryName();
        return !HIGH_MAGIC_MODS.contains(itemResource.getResourceDomain()) ||
                MAGIC_ITEM_ALLOW_USE.contains(itemResource.toString());
    }
    
    public static boolean isCraftingAllowed(ItemStack itemStack) {
        Item item = itemStack.getItem();
        
        ResourceLocation itemResource = item.getRegistryName();
        return !HIGH_MAGIC_MODS.contains(itemResource.getResourceDomain()) ||
                    MAGIC_ITEM_ALLOW_CRAFT.contains(itemResource.toString());
    }
    
    public static boolean isUseAllowed(Block block) {
        if (!Interaction.hasSpecialUse(block)) {
            return true;
        }
        
        ResourceLocation blockResource = block.getRegistryName();
        return !HIGH_MAGIC_MODS.contains(blockResource.getResourceDomain()) ||
                    MAGIC_BLOCK_ALLOW_USE.contains(blockResource.toString());
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
    }
    
    //TODO: Fork Blood Magic for 1.10 and properly fix this bug
    /** The dead player that Blood Magic uses for caching has an
     * old version of the MISC_CAPABILITY, which we need in order
     * to figure out if the player has died since a ritual
     * was activated. See ListenerPlayerMagic.onRunPastLifeBloodRitual
     */
    @Optional.Method(modid=ModState.BLOOD_MAGIC_ID)
    public static void fixSoulNetworkCachingDeadPlayer(EntityPlayer player) {
        SoulNetwork soulNetwork = NetworkHelper.getSoulNetwork(player);
        if (soulNetwork == null) {
            return;
        }
        try {
            Field cachedPlayerField = SoulNetwork.class.getDeclaredField("cachedPlayer");
            cachedPlayerField.setAccessible(true);
            cachedPlayerField.set(soulNetwork, null);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
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
    
    @SubscribeEvent
    @Optional.Method(modid=ModState.BLOOD_MAGIC_ID)
    public void onStartBloodRitual(RitualActivatedEvent event) {
        SoulNetwork soulNetwork = NetworkHelper.getSoulNetwork(event.ownerName);
        if (soulNetwork == null) {
            return;
        }
        EntityPlayer player = soulNetwork.getPlayer();
        if (player == null) {
            return;
        }
        ICapabilityMisc misc = player.getCapability(MISC_CAPABILITY, null);
        if (misc == null) {
            return;
        }
        UUID lifetimeUUID = misc.getLifetimeUUID();
        if (!(event.mrs instanceof TileMasterRitualStone)) {
            return;
        }
        ICapabilityTileHistory tileHistory = ((TileMasterRitualStone)event.mrs).getCapability(TILE_HISTORY_CAPABILITY, null);
        if (tileHistory == null) {
            return;
        }
        tileHistory.setOwnerLifetimeUUID(lifetimeUUID);
    }

    @SubscribeEvent
    @Optional.Method(modid=ModState.BLOOD_MAGIC_ID)
    public void onRunPastLifeBloodRitual(RitualRunEvent event) {
        SoulNetwork soulNetwork = NetworkHelper.getSoulNetwork(event.ownerName);
        if (soulNetwork == null) {
            return;
        }
        EntityPlayer player = soulNetwork.getPlayer();
        if (player == null) {
            return;
        }
        ICapabilityMisc misc = player.getCapability(MISC_CAPABILITY, null);
        if (misc == null) {
            return;
        }
        UUID lifetimeUUID = misc.getLifetimeUUID();
        if (!(event.mrs instanceof TileMasterRitualStone)) {
            return;
        }
        ICapabilityTileHistory tileHistory = ((TileMasterRitualStone)event.mrs).getCapability(TILE_HISTORY_CAPABILITY, null);
        if (tileHistory == null) {
            return;
        }
        if (tileHistory.getOwnerLifetimeUUID() == null) {
            return;
        }
        if (!(tileHistory.getOwnerLifetimeUUID().equals(lifetimeUUID))) {
            // This Master Ritual Stone was activated in a past life, so it should no longer work
            event.setCanceled(true);
            event.mrs.stopRitual(BreakType.DEACTIVATE);
        }
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
