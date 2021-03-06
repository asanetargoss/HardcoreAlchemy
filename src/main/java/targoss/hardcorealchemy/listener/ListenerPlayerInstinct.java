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

import java.util.List;
import java.util.Map;
import java.util.Random;

import mchorse.metamorph.api.MorphManager;
import mchorse.metamorph.api.abilities.IAction;
import mchorse.metamorph.api.morphs.AbstractMorph;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import targoss.hardcorealchemy.capability.instinct.ICapabilityInstinct;
import targoss.hardcorealchemy.config.Configs;
import targoss.hardcorealchemy.instinct.InstinctEffectOverheat;
import targoss.hardcorealchemy.instinct.InstinctEffectTemperedFlame;
import targoss.hardcorealchemy.instinct.Instincts;
import targoss.hardcorealchemy.instinct.api.IInstinctEffectData;
import targoss.hardcorealchemy.instinct.internal.InstinctEffectWrapper;
import targoss.hardcorealchemy.instinct.internal.InstinctNeedWrapper;
import targoss.hardcorealchemy.instinct.internal.InstinctState;
import targoss.hardcorealchemy.instinct.internal.InstinctSystem;
import targoss.hardcorealchemy.util.InventoryUtil;

/**
 * Capability handling, ticking, and event hooks for instincts.
 */
public class ListenerPlayerInstinct extends ConfiguredListener {
    protected InstinctSystem instinctSystem;
    
    public ListenerPlayerInstinct(Configs configs) {
        super(configs);
        instinctSystem = new InstinctSystem(configs);
    }
    
    @CapabilityInject(ICapabilityInstinct.class)
    public static final Capability<ICapabilityInstinct> INSTINCT_CAPABILITY = null;
    
    public static class ColdLimitedAction implements IAction {
        protected IAction delegate;
        
        protected ThreadLocal<Random> random = new ThreadLocal<>();

        public ColdLimitedAction(IAction delegate) {
            this.delegate = delegate;
        }
        
        public static void wrapAction(Map<String, IAction> actions, String actionName) {
            IAction action = actions.get(actionName);
            if (action != null) {
                actions.put(actionName, new ColdLimitedAction(action));
            }
        }
        
        protected static boolean isHeatHindered(EntityLivingBase entity) {
            ICapabilityInstinct instinct = entity.getCapability(INSTINCT_CAPABILITY, null);
            if (instinct == null) {
                return false;
            }
            IInstinctEffectData data = instinct.getInstinctEffectData(Instincts.EFFECT_OVERHEAT);
            if (!(data instanceof InstinctEffectOverheat.Data)) {
                return false;
            }
            InstinctEffectOverheat.Data overheatData = (InstinctEffectOverheat.Data)data;
            if (overheatData.isOverheating()) {
                return false;
            }
            InstinctEffectWrapper wrapper = instinct.getActiveEffects().get(Instincts.EFFECT_TEMPERED_FLAME);
            if (wrapper == null) {
                return false;
            }
            IInstinctEffectData data2 = instinct.getInstinctEffectData(Instincts.EFFECT_TEMPERED_FLAME);
            if (!(data2 instanceof InstinctEffectTemperedFlame.Data)) {
                return false;
            }
            InstinctEffectTemperedFlame.Data temperedFlameData = (InstinctEffectTemperedFlame.Data)data2;
            return InstinctEffectTemperedFlame.getMaxAllowedColdAmplifier(temperedFlameData, wrapper.amplifier) >= 1.0F;
        }

        @Override
        public void execute(EntityLivingBase entity, AbstractMorph morph) {
            if (!isHeatHindered(entity)) {
                delegate.execute(entity, morph);
            } else {
                Random localRandom = random.get();
                if (localRandom == null) {
                    localRandom = new Random();
                    random.set(localRandom);
                }
                entity.world.playSound(null, entity.getPosition(), SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS,
                        0.5F, 2.6F + (localRandom.nextFloat() - localRandom.nextFloat()) * 0.8F);
            }
        }
    }
    
    @Override
    public void postInit(FMLPostInitializationEvent event) {
        Map<String, IAction> actions = MorphManager.INSTANCE.actions;
        ColdLimitedAction.wrapAction(actions, "fireball");
        ColdLimitedAction.wrapAction(actions, "small_fireball");
        ColdLimitedAction.wrapAction(actions, "fire_breath");
    }
    
    /**
     * See also: ListenerPlayerHumanity.onPlayerEnterAfterlife
     * */
    @SubscribeEvent
    public void onPlayerDie(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof EntityPlayer)) {
            return;
        }
        
        EntityPlayer player = (EntityPlayer)(event.getEntity());
        ICapabilityInstinct instinct = player.getCapability(INSTINCT_CAPABILITY, null);
        if (instinct == null) {
            return;
        }
        instinct.clearInstincts(player);
    }
    
    /**
     * Instinct ticking.
     * All instinct needs and active instinct effects are ticked.
     * The instinct system updates needs and effects.
     */
    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent event) {
        if (event.phase != Phase.START) {
            return;
        }
        EntityPlayer player = event.player;
        ICapabilityInstinct instinct = player.getCapability(INSTINCT_CAPABILITY, null);
        if (instinct == null) {
            return;
        }
        
        for (ICapabilityInstinct.InstinctEntry entry : instinct.getInstincts()) {
            for (InstinctNeedWrapper needWrapper : entry.getNeeds(player)) {
                InstinctState instinctState = needWrapper.getState(player);
                needWrapper.getNeed(player).tick(instinctState);
            }
        }
        
        instinctSystem.tickPlayer(configs, instinct, event);
        
        for (InstinctEffectWrapper effect : instinct.getActiveEffects().values()) {
            effect.effect.tick(player, effect.amplifier);
        }
    }
    
    @SubscribeEvent
    public void onBlockInteract(PlayerInteractEvent.RightClickBlock event) {
        EntityPlayer player = event.getEntityPlayer();
        ICapabilityInstinct instinct = player.getCapability(INSTINCT_CAPABILITY, null);
        if (instinct == null) {
            return;
        }
        
        BlockPos blockPos = event.getPos();
        Block block = player.world.getBlockState(blockPos).getBlock();
        for (InstinctEffectWrapper effect : instinct.getActiveEffects().values()) {
            if (!effect.effect.canInteract(player, effect.amplifier, blockPos, block)) {
                event.setUseBlock(Result.DENY);
                break;
            }
        }
        
        ItemStack itemStack = event.getItemStack();
        if (itemStack != InventoryUtil.ITEM_STACK_EMPTY) {
            Item item = itemStack.getItem();
            if (item instanceof ItemBlock) {
                Block blockFromItem = ((ItemBlock)item).getBlock();
                BlockPos placeBlockPos = blockPos.offset(event.getFace());
                for (InstinctEffectWrapper effect : instinct.getActiveEffects().values()) {
                    if (!effect.effect.canPlaceBlock(player, effect.amplifier, placeBlockPos, blockFromItem)) {
                        event.setUseItem(Result.DENY);
                        break;
                    }
                }
            }
        }
    }
    
    @SubscribeEvent
    public void onItemInteract(PlayerInteractEvent.RightClickItem event) {
        EntityPlayer player = event.getEntityPlayer();
        ICapabilityInstinct instinct = player.getCapability(INSTINCT_CAPABILITY, null);
        if (instinct == null) {
            return;
        }
        
        ItemStack itemStack = event.getItemStack();
        for (InstinctEffectWrapper effect : instinct.getActiveEffects().values()) {
            if (!effect.effect.canInteract(player, effect.amplifier, itemStack)) {
                event.setCanceled(true);
                break;
            }
        }
    }
    
    private static boolean isFlammableLivingEntity(Entity entity) {
        return !entity.isImmuneToFire() && (entity instanceof EntityLivingBase) && EntitySelectors.NOT_SPECTATING.apply(entity);
    }
    
    @SubscribeEvent
    public void onPlayerIgnitePre(PlayerInteractEvent.RightClickBlock event) {
        // TODO: Lava
        // TODO Electric Boogaloo: Simulate entities in fake world to see if they get damaged, then cache the result
        EntityPlayer player = event.getEntityPlayer();
        ICapabilityInstinct instinct = player.getCapability(INSTINCT_CAPABILITY, null);
        if (instinct == null) {
            return;
        }
        
        ItemStack itemStack = event.getItemStack();
        if (InventoryUtil.isEmptyItemStack(itemStack) || itemStack.getItem() != Items.FLINT_AND_STEEL) {
            return;
        }
        
        EnumFacing facing = event.getFace();
        if (facing == null) {
            return;
        }
        
        BlockPos pos = event.getPos();
        
        BlockPos firePos = pos.offset(facing);
        // Check 3x3 area for nearby mobs that might be set on fire
        AxisAlignedBB aabb = new AxisAlignedBB(firePos.getX() - 1, firePos.getY() - 1, firePos.getZ() - 1,
                firePos.getX() + 2, firePos.getY() + 2, firePos.getZ() + 2);
        
    checkEntities:
        for (Entity entity : player.world.getEntitiesInAABBexcluding(player, aabb, ListenerPlayerInstinct::isFlammableLivingEntity)) {
            EntityLivingBase entityLiving = (EntityLivingBase)entity;
            for (InstinctEffectWrapper effect : instinct.getActiveEffects().values()) {
                if (!effect.effect.canAttack(player, effect.amplifier, entityLiving)) {
                    event.setUseItem(Result.DENY);
                    break checkEntities;
                }
            }
        }
    }
    
    @SubscribeEvent
    public void onPlayerAttackPre(LivingAttackEvent event) {
        if (event.getEntityLiving().world.isRemote) {
            return;
        }
        
        /*
         * Ideally we want the player to not show an attack animation,
         * but for ranged weapons it's hard to know if the entity will
         * be hit in advance.
         * Not realistic, but prevents exploits.
         */
        DamageSource damageSource = event.getSource();
        if (!(damageSource instanceof EntityDamageSource)) {
            return;
        }
        Entity agressor = ((EntityDamageSource)damageSource).getEntity();
        if (!(agressor instanceof EntityPlayer)) {
            return;
        }
        EntityPlayer player = (EntityPlayer)agressor;
        
        ICapabilityInstinct instinct = player.getCapability(INSTINCT_CAPABILITY, null);
        if (instinct == null) {
            return;
        }
        
        EntityLivingBase entityLiving = event.getEntityLiving();
        for (InstinctEffectWrapper effect : instinct.getActiveEffects().values()) {
            if (!effect.effect.canAttack(player, effect.amplifier, entityLiving)) {
                event.setCanceled(true);
                break;
            }
        }
    }
    
    @SubscribeEvent
    public void onPlayerKill(LivingDeathEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (entity.world.isRemote) {
            return;
        }
        
        DamageSource damageSource = event.getSource();
        if (damageSource == null || !(damageSource instanceof EntityDamageSource)) {
            // No excuse at this point.
            return;
        }
        
        Entity agressor = ((EntityDamageSource)damageSource).getEntity();
        if (!(agressor instanceof EntityPlayer)) {
            return;
        }
        
        EntityPlayer player = (EntityPlayer)agressor;
        
        ICapabilityInstinct instinct = agressor.getCapability(INSTINCT_CAPABILITY, null);
        if (instinct == null) {
            return;
        }
        
        List<ICapabilityInstinct.InstinctEntry> instincts = instinct.getInstincts();
        for (ICapabilityInstinct.InstinctEntry entry : instincts) {
            for (InstinctNeedWrapper needWrapper : entry.getNeeds(player)) {
                needWrapper.getNeed(player).afterKill(needWrapper.getState(player), entity);
            }
        }
    }
    
    @SubscribeEvent
    public void onPlayerDig(PlayerEvent.BreakSpeed event) {
        EntityPlayer player = event.getEntityPlayer();
        
        ICapabilityInstinct instinct = player.getCapability(INSTINCT_CAPABILITY, null);
        if (instinct == null) {
            return;
        }
        
        ItemStack heldItem = player.getHeldItemMainhand();
        IBlockState state = event.getState();
        for (InstinctEffectWrapper effect : instinct.getActiveEffects().values()) {
            if (!effect.effect.canInteract(player, effect.amplifier, heldItem)) {
                if (!InventoryUtil.isEmptyItemStack(heldItem)) {
                    float toolBreakStrength = player.inventory.getStrVsBlock(state);
                    if (toolBreakStrength > 1.0F) {
                        int efficiencyModifier = EnchantmentHelper.getEfficiencyModifier(player);
                        if (efficiencyModifier > 0)
                        {
                            toolBreakStrength += (float)(efficiencyModifier * efficiencyModifier + 1);
                        }
                    }
                    if (toolBreakStrength != 0.0F) {
                        event.setNewSpeed(event.getOriginalSpeed() / toolBreakStrength);
                    }
                }
                break;
            }
        }
    }
    
    @SubscribeEvent
    public void onPlayerDigPost(HarvestDropsEvent event) {
        EntityPlayer player = event.getHarvester();
        if (player == null) {
            return;
        }
        
        ICapabilityInstinct instinct = player.getCapability(INSTINCT_CAPABILITY, null);
        if (instinct == null) {
            return;
        }
        
        for (InstinctEffectWrapper effect : instinct.getActiveEffects().values()) {
            effect.effect.onBlockDrops(player, effect.amplifier, event);
        }
    }
    
    @SubscribeEvent
    public void onPlayerKillEntityForDrops(LivingDropsEvent event) {
        Entity killer = event.getEntity();
        if (killer.world.isRemote) {
            return;
        }
        
        if (!(killer instanceof EntityPlayer)) {
            return;
        }
        EntityPlayer player = (EntityPlayer)killer;
        
        ICapabilityInstinct instinct = player.getCapability(INSTINCT_CAPABILITY, null);
        if (instinct == null) {
            return;
        }
        
        for (InstinctEffectWrapper effect : instinct.getActiveEffects().values()) {
            effect.effect.onEntityDrops(player, effect.amplifier, event);
        }
    }
    
}
