package targoss.hardcorealchemy.listener;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import WayofTime.bloodmagic.api.saving.SoulNetwork;
import WayofTime.bloodmagic.api.util.helper.NetworkHelper;
import am2.api.affinity.Affinity;
import am2.api.event.SpellCastEvent;
import am2.api.extensions.IAffinityData;
import am2.api.extensions.IEntityExtension;
import am2.api.extensions.ISkillData;
import am2.api.skill.Skill;
import am2.api.skill.SkillPoint;
import am2.extensions.AffinityData;
import am2.extensions.EntityExtension;
import am2.extensions.SkillData;
import am2.spell.ContingencyType;
import mchorse.metamorph.api.MorphAPI;
import mchorse.metamorph.api.morphs.AbstractMorph;
import mchorse.metamorph.capabilities.morphing.IMorphing;
import mchorse.metamorph.capabilities.morphing.Morphing;
import moze_intel.projecte.api.ProjectEAPI;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.registry.GameRegistry;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.capability.humanity.ICapabilityHumanity;
import targoss.hardcorealchemy.event.EventTakeStack;
import targoss.hardcorealchemy.network.MessageHumanity;
import targoss.hardcorealchemy.network.MessageMagic;
import targoss.hardcorealchemy.network.PacketHandler;
import targoss.hardcorealchemy.util.Chat;

public class ListenerPlayerMagic {
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
    
    // Client-side flag. The humanity capability is not used on the client-side, so this value is authoritative.
    public static boolean canUseHighMagic = false;
    
    static {
        HIGH_MAGIC_MODS = new HashSet<String>();
        HIGH_MAGIC_MODS.add("arsmagica2");
        HIGH_MAGIC_MODS.add("ProjectE");
        HIGH_MAGIC_MODS.add("projecte");
        HIGH_MAGIC_MODS.add("astralsorcery");
        
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
    
    //TODO: Use Java reflection to check if these items/blocks even have a use to begin with, and only notify chat if that is the case
    
    /*TODO: Prevent using block transmutation feature of Philosopher Stone
     */
    
    @SubscribeEvent
    public void onPlayerTickMP(TickEvent.PlayerTickEvent event) {
        if (event.phase != Phase.END) {
            return;
        }
        EntityPlayer player = event.player;
        if (player.worldObj.isRemote) {
            return;
        }
        ICapabilityHumanity capabilityHumanity = player.getCapability(HUMANITY_CAPABILITY, null);
        if (capabilityHumanity != null) {
            capabilityHumanity.setNotifiedMagicFail(false);
        }
    }
    
    @SubscribeEvent
    public void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        EntityPlayer player = event.getEntityPlayer();
        if (player.worldObj.isRemote) {
            if (canUseHighMagic) {
                return;
            }
            if (!isAllowed(MAGIC_ITEM_ALLOW_USE, event.getItemStack())) {
                event.setCanceled(true);
            }
        }
        else {
            ICapabilityHumanity capabilityHumanity = player.getCapability(HUMANITY_CAPABILITY, null);
            if (capabilityHumanity == null || capabilityHumanity.canUseHighMagic()) {
                return;
            }
            if (!isAllowed(MAGIC_ITEM_ALLOW_USE, event.getItemStack())) {
                event.setCanceled(true);
                if (!capabilityHumanity.getNotifiedMagicFail()) {
                    capabilityHumanity.setNotifiedMagicFail(true);
                    Chat.notify((EntityPlayerMP)player, new TextComponentTranslation("hardcorealchemy.magic.disabled.item"));
                }
            }
        }
    }
    
    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        World world = event.getWorld();
        Block block = world.getBlockState(event.getPos()).getBlock();
        EntityPlayer player = event.getEntityPlayer();
        if (world.isRemote) {
            if (canUseHighMagic) {
                return;
            }
            if (!isAllowed(MAGIC_BLOCK_ALLOW_USE, block)) {
                event.setCanceled(true);
            }
        }
        else {
            ICapabilityHumanity capabilityHumanity = player.getCapability(HUMANITY_CAPABILITY, null);
            if (capabilityHumanity == null || capabilityHumanity.canUseHighMagic()) {
                return;
            }
            if (!isAllowed(MAGIC_BLOCK_ALLOW_USE, block)) {
                event.setCanceled(true);
                if (!capabilityHumanity.getNotifiedMagicFail()) {
                    capabilityHumanity.setNotifiedMagicFail(true);
                    Chat.notify((EntityPlayerMP)player, new TextComponentTranslation("hardcorealchemy.magic.disabled.block"));
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
        if (player.worldObj.isRemote) {
            if (!canUseHighMagic && !isAllowed(MAGIC_ITEM_ALLOW_CRAFT, craftResult)) {
                event.setCanceled(true);
            }
        }
        else {
            ICapabilityHumanity capabilityHumanity = player.getCapability(HUMANITY_CAPABILITY, null);
            if (capabilityHumanity != null &&
                    !capabilityHumanity.canUseHighMagic() &&
                    !isAllowed(MAGIC_ITEM_ALLOW_CRAFT, craftResult)) {
                event.setCanceled(true);
                if (!capabilityHumanity.getNotifiedMagicFail()) {
                    capabilityHumanity.setNotifiedMagicFail(true);
                    Chat.notify((EntityPlayerMP)player, new TextComponentTranslation("hardcorealchemy.magic.disabled.craft"));
                }
            }
        }
    }
    
    // When a player chooses the path of a spellcaster, they lose the ability to morph
    @Optional.Method(modid = HardcoreAlchemy.ARS_MAGICA_ID)
    @SubscribeEvent
    public void onCastFirstSpell(SpellCastEvent.Pre event) {
        EntityLivingBase entity = event.entityLiving;
        if (entity.worldObj.isRemote || !(entity instanceof EntityPlayerMP)) {
            return;
        }
        EntityPlayerMP player = (EntityPlayerMP)entity;
        ICapabilityHumanity humanityCapability = player.getCapability(HUMANITY_CAPABILITY, null);
        if (humanityCapability != null && !humanityCapability.getIsMage()) {
            IMorphing morphing = Morphing.get(player);
            if (morphing != null) {
                AbstractMorph morph = morphing.getCurrentMorph();
                /* Casting a spell makes you lose the ability to morph.
                 * Normally, this makes you stuck as a human.
                 * However, some morphs can still use spells, resulting
                 * in the player being stuck as that morph.
                 */
                if (morph != null) {
                    if (ListenerPlayerHumanity.HIGH_MAGIC_MORPHS.contains(morph.name)) {
                        // If a player is stuck in a morph, they should by definition have no humanity
                        humanityCapability.setHumanity(0.0D);
                        // Prevents setting forced morph flag hasLostHumanity. I REALLY need to refactor forced morphs/being stuck as a human
                        humanityCapability.setLastHumanity(0.0D); 
                    }
                    else {
                        MorphAPI.demorph(player);
                    }
                }
            }
            humanityCapability.setIsMage(true);
            Chat.notifyMagical(player, new TextComponentTranslation("hardcorealchemy.magic.becomemage"));
        }
    }
    
    @SubscribeEvent
    public void onMageDie(PlayerRespawnEvent event) {
        if (event.player.worldObj.isRemote) {
            return;
        }
        
        eraseAllMortalMagic((EntityPlayerMP)(event.player));
    }
    
    public static boolean isAllowed(Set<String> whitelist, ItemStack itemStack) {
        ResourceLocation itemResource = itemStack.getItem().getRegistryName();
        return !HIGH_MAGIC_MODS.contains(itemResource.getResourceDomain()) ||
                    whitelist.contains(itemResource.toString());
    }
    
    public static boolean isAllowed(Set<String> whitelist, Block block) {
        ResourceLocation blockResource = block.getRegistryName();
        return !HIGH_MAGIC_MODS.contains(blockResource.getResourceDomain()) ||
                    whitelist.contains(blockResource.toString());
    }
    
    public static void eraseAllMortalMagic(EntityPlayerMP player) {
        if (HardcoreAlchemy.isBloodMagicLoaded) {
            eraseBloodMagic(player);
        }
        if (HardcoreAlchemy.isArsMagicaLoaded) {
            eraseSpellMagic(player);
        }
        if (HardcoreAlchemy.isProjectELoaded) {
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
    @Optional.Method(modid = HardcoreAlchemy.BLOOD_MAGIC_ID)
    public static void eraseBloodMagic(EntityPlayerMP player) {
        SoulNetwork network = NetworkHelper.getSoulNetwork(player);
        network.setOrbTier(0);
        network.setCurrentEssence(0);
    }
    
    /*
     * What it does: Resets magic level, skill allocations, and affinities
     * Why it does it: 1) Prevents affinity effects from persisting across lives or
     * (In the case of ListenerPlayerHumanity) into permanent morphs
     * 2) Sets other aspects of the player's magic to be consistent with that fact
     * 3) Allow for the player to try new specializations
     */
    @Optional.Method(modid = HardcoreAlchemy.ARS_MAGICA_ID)
    public static void eraseSpellMagic(EntityPlayerMP player) {
        
        IEntityExtension playerMagicExtension = EntityExtension.For(player);
        if (playerMagicExtension != null) {
            /* Ars Magica seems to randomly forget to send players their magic level
             * when you die. (took me a while to figure out it wasn't this method's code)
             * Not good!
             * Workaround: Cast enough spells to raise your magic level
             * TODO: Test Ars Magica by itself to verify it is a bug from that
             * mod specifically
             */
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
    @Optional.Method(modid = HardcoreAlchemy.PROJECT_E_ID)
    public static void eraseEMC(EntityPlayerMP player) {
        IKnowledgeProvider transmutationKnowledge = player.getCapability(ProjectEAPI.KNOWLEDGE_CAPABILITY, null);
        if (transmutationKnowledge != null) {
            transmutationKnowledge.setEmc(0.0D);
        }
    }
}
