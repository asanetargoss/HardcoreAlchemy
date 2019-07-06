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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import mchorse.metamorph.Metamorph;
import mchorse.metamorph.capabilities.morphing.IMorphing;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.Clone;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import targoss.hardcorealchemy.capability.CapUtil;
import targoss.hardcorealchemy.capability.instinct.CapabilityInstinct;
import targoss.hardcorealchemy.capability.instinct.ICapabilityInstinct;
import targoss.hardcorealchemy.capability.instinct.ProviderInstinct;
import targoss.hardcorealchemy.config.Configs;
import targoss.hardcorealchemy.instinct.api.IInstinctState;
import targoss.hardcorealchemy.instinct.api.InstinctEffect;
import targoss.hardcorealchemy.instinct.api.InstinctEffectWrapper;
import targoss.hardcorealchemy.instinct.api.InstinctNeedWrapper;
import targoss.hardcorealchemy.instinct.api.InstinctState;
import targoss.hardcorealchemy.network.MessageInstinctEffects;
import targoss.hardcorealchemy.network.MessageInstinctNeedChanged;
import targoss.hardcorealchemy.network.MessageInstinctNeedState;
import targoss.hardcorealchemy.network.PacketHandler;
import targoss.hardcorealchemy.util.Chat;
import targoss.hardcorealchemy.util.InventoryUtil;
import targoss.hardcorealchemy.util.MorphState;

/**
 * Capability handling, ticking, and event hooks for instincts.
 */
public class ListenerPlayerInstinct extends ConfiguredListener {
    public ListenerPlayerInstinct(Configs configs) {
        super(configs);
    }
    
    @CapabilityInject(ICapabilityInstinct.class)
    public static final Capability<ICapabilityInstinct> INSTINCT_CAPABILITY = null;
    /** Fractional amount of instinct lost per tick. One instinct icon lasts 2 days */
    public static final double INSTINCT_LOSS_RATE = 2.0D/24000.0D/2.0D;
    /** If an instinct check fails, the amount of ticks that must pass before another check can occur */
    public static final int INSTINCT_CHECK_INTERVAL = 24000 / 4;
    /** The time in ticks that must pass before a message is displayed that
     * a need became worse. Reset when the instinct bar is full */
    public static final int NEED_CHANGE_MESSAGE_MIN_TIME = 200;
    
    private Random random = new Random();

    @SubscribeEvent
    public void onAttachCapability(AttachCapabilitiesEvent<Entity> event) {
        if (!(event.getObject() instanceof EntityPlayer) || (event.getObject() instanceof FakePlayer)) {
            return;
        }
        
        event.addCapability(CapabilityInstinct.RESOURCE_LOCATION, new ProviderInstinct());
        AbstractAttributeMap attributeMap = ((EntityPlayer)event.getObject()).getAttributeMap();
        if (attributeMap.getAttributeInstance(ICapabilityInstinct.MAX_INSTINCT) == null) {
            attributeMap.registerAttribute(ICapabilityInstinct.MAX_INSTINCT);
        }
    }
    
    @SubscribeEvent
    public void onPlayerClone(Clone event) {
        if (!event.isWasDeath() || Metamorph.proxy.config.keep_morphs) {
            EntityPlayer player = event.getEntityPlayer();
            EntityPlayer playerOld = event.getOriginal();
            CapUtil.copyOldToNew(INSTINCT_CAPABILITY, playerOld, player);
        }
    }
    
    /**
     * See also: ListenerPlayerHumanity.onPlayerEnterAfterlife
     * */
    @SubscribeEvent
    public void onPlayerDie(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof EntityPlayer)) {
            return;
        }
        if (Metamorph.proxy.config.keep_morphs) {
            return;
        }
        
        EntityPlayer player = (EntityPlayer)(event.getEntity());
        ICapabilityInstinct instinct = player.getCapability(INSTINCT_CAPABILITY, null);
        if (instinct == null) {
            return;
        }
        instinct.clearInstincts(player);
    }
    
    public static void addInstinct(EntityPlayer player, ICapabilityInstinct instinct, float instinctChange) {
        float maxInstinct;
        IAttributeInstance maxInstinctAttribute = player.getEntityAttribute(ICapabilityInstinct.MAX_INSTINCT);
        if (maxInstinctAttribute != null) {
            maxInstinct = (float)maxInstinctAttribute.getAttributeValue();
        }
        else {
            maxInstinct = 20.0F;
        }
        instinct.setInstinct(MathHelper.clamp(instinct.getInstinct() + instinctChange, 0.0F, maxInstinct));
    }
    
    /**
     * Instinct ticking.
     * All instinct needs and active instinct effects are ticked.
     * Assuming all needs are fulfilled, the instinct bar will replenish.
     * Otherwise, the most urgent current need dictates whether the instinct bar holds steady or decreases.
     * If the instinct bar drops below a certain value, InstinctEffects may be applied
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
        
        // Rather than bail out at the beginning, check if some instinct effects are enabled, as they may alter state.
        if (!configs.base.enableInstincts) {
            if (instinct.getEnabled()) {
                instinct.clearInstincts(player);
                instinct.setInstinct(ICapabilityInstinct.DEFAULT_INSTINCT_VALUE);
                instinct.setEnabled(false);
            }
            return;
        } else {
            if (!instinct.getEnabled()) {
                // Need to re-initialize
                MorphState.buildInstincts(player, instinct);
                instinct.setEnabled(true);
            }
        }

        List<ICapabilityInstinct.InstinctEntry> entries = instinct.getInstincts();
        if (entries.size() == 0 && instinct.getActiveEffects().size() == 0) {
            return;
        }
        
        float currentInstinct = instinct.getInstinct();
        float instinctChange = InstinctState.getInstinctChangePerTick(IInstinctState.NeedStatus.NONE);
        
        // Check needs
        
        // Tick each need, get the associated instinct change, and sync if needed
        boolean needStatusChanged = false;
        int entryCount = 0;
        int needCount = 0;
        for (ICapabilityInstinct.InstinctEntry entry : instinct.getInstincts()) {
            for (InstinctNeedWrapper needWrapper : entry.getNeeds(player)) {
                InstinctState instinctState = needWrapper.getState(player);
                
                needWrapper.getNeed(player).tick(instinctState);
                needStatusChanged |= instinctState.needStatus != instinctState.lastNeedStatus;
                instinctState.lastNeedStatus = instinctState.needStatus;
                
                instinctChange = Math.min(instinctChange, instinctState.getInstinctChangePerTick());
                
                if (!player.world.isRemote && instinctState.messenger.shouldSync()) {
                    PacketHandler.INSTANCE.sendTo(new MessageInstinctNeedChanged(entryCount, needCount, needWrapper), (EntityPlayerMP)player);
                    instinctState.shouldSyncNeed = false;
                }
                needCount++;
            }
            entryCount++;
        }
        if (needStatusChanged && !player.world.isRemote) {
            PacketHandler.INSTANCE.sendTo(new MessageInstinctNeedState(instinct), (EntityPlayerMP)player);
        }
        
        // Display messages that tell the player information about the instincts that affect them
        if (!player.world.isRemote) {
            displayNeedMessages((EntityPlayerMP)player, instinct);
        }
        
        // Update the active effects, calling activation/deactivation functions as necessary.
        // Effect changes are handled differentially to make efficient network syncing easy.
        Map<InstinctEffect, InstinctEffectWrapper> oldEffects = instinct.getActiveEffects();
        Map<InstinctEffect, InstinctEffectWrapper> newEffects = computeNewActiveEffects(player, instinct);
        Map<InstinctEffect, InstinctEffectWrapper> effectChanges = getEffectChanges(oldEffects, newEffects);
        transitionEffects(player, instinct, effectChanges);
        if (!player.world.isRemote && effectChanges.size() != 0) {
            PacketHandler.INSTANCE.sendTo(new MessageInstinctEffects(effectChanges), (EntityPlayerMP)player);
        }
        
        // Tick active instinct effects
        for (InstinctEffectWrapper effect : instinct.getActiveEffects().values()) {
            effect.effect.tick(player, effect.amplifier);
        }
        
        addInstinct(player, instinct, instinctChange);
    }
    
    public static void displayNeedMessages(EntityPlayerMP player, ICapabilityInstinct instinct) {
        // Display a periodic need message about what one of the needs does
        int instinctMessageTime = instinct.getInstinctMessageTime();
        if (instinctMessageTime >= INSTINCT_CHECK_INTERVAL) {
            sendRandomInstinctToChat(player, instinct);
            instinctMessageTime = 0;
        }
        else {
            instinctMessageTime++;
        }
        instinct.setInstinctMessageTime(instinctMessageTime);
        
        // If a need becomes more urgent, give an opportunity to display a message about the need
        float maxInstinct = (float)ICapabilityInstinct.MAX_INSTINCT.getDefaultValue();
        IAttributeInstance maxInstinctAttribute = player.getEntityAttribute(ICapabilityInstinct.MAX_INSTINCT);
        if (maxInstinctAttribute != null) {
            maxInstinct = (float)maxInstinctAttribute.getAttributeValue();
        }
        if (instinct.getInstinct() >= maxInstinct) {
            // The player's instinct bar is full. Reset things so need messages can be displayed again.
            for (ICapabilityInstinct.InstinctEntry entry : instinct.getInstincts()) {
                for (InstinctNeedWrapper needWrapper : entry.getNeeds(player)) {
                    needWrapper.mostSevereStatusSinceMessage = InstinctState.NeedStatus.NONE;
                    needWrapper.playerTickSinceInstinctFull = player.ticksExisted;
                }
            }
        }
        else {
            // For each need, check if the need status has changed, and if so, allow it to display a message.
            for (ICapabilityInstinct.InstinctEntry entry : instinct.getInstincts()) {
                for (InstinctNeedWrapper needWrapper : entry.getNeeds(player)) {
                    if (needWrapper.playerTickSinceInstinctFull > player.ticksExisted) {
                        // This doesn't make sense
                        needWrapper.playerTickSinceInstinctFull = player.ticksExisted;
                    }
                    if (player.ticksExisted - needWrapper.playerTickSinceInstinctFull < NEED_CHANGE_MESSAGE_MIN_TIME) {
                        // Too early; throttle messages
                        return;
                    }
                    if (needWrapper.state.needStatus.ordinal() <= needWrapper.mostSevereStatusSinceMessage.ordinal()) {
                        // Not more severe since the instinct bar was full
                        return;
                    }
                    
                    needWrapper.mostSevereStatusSinceMessage = needWrapper.state.needStatus;
                    needWrapper.playerTickSinceInstinctFull = player.ticksExisted;
                    ITextComponent needMessage = needWrapper.getNeed(player).getNeedUnfulfilledMessage(needWrapper.state.needStatus);
                    if (needMessage != null) {
                        Chat.message(Chat.Type.NOTIFY, player, needMessage);
                    }
                }
            }
        }
    }
    
    public static Map<InstinctEffect, InstinctEffectWrapper> computeNewActiveEffects(EntityPlayer player, ICapabilityInstinct instinct) {
        float currentInstinct = instinct.getInstinct();
        Map<InstinctEffect, InstinctEffectWrapper> pastEffects = instinct.getActiveEffects();
        Map<InstinctEffect, InstinctEffectWrapper> newEffects = new HashMap<>();
        
        for (ICapabilityInstinct.InstinctEntry entry : instinct.getInstincts()) {
            // First, see if any needs in this instinct are not being met
            // If all needs are satisfied, new effects will not be activated
            // Note that existing effects will remain active with the
            //  current amplifier or higher unless instinct exceeds the maxInstinct of all candidate effects of the same type
            boolean needsMet = true;
            for (InstinctNeedWrapper needWrapper : entry.getNeeds(player)) {
                needsMet &= needWrapper.state.needStatus == IInstinctState.NeedStatus.NONE;
            }
            
            // Then find all the effect amplifiers to apply
            Map<InstinctEffect, Float> effectAmplifiers = new HashMap<>();
            for (InstinctNeedWrapper needWrapper : entry.getNeeds(player)) {
                for (Map.Entry<InstinctEffect, Float> amplifierEntry : needWrapper.state.effectAmplifiers.entrySet()) {
                    InstinctEffect effect = amplifierEntry.getKey();
                    Float amplifier = effectAmplifiers.get(effect);
                    if (amplifier == null) {
                        amplifier = amplifierEntry.getValue();
                    }
                    else {
                        amplifier = Math.max(amplifier, amplifierEntry.getValue());
                    }
                    effectAmplifiers.put(effect, amplifier);
                }
            }
            
            // Then, see if we actually want to apply each effect
        checkEffect:
            for (InstinctEffectWrapper effectWrapper : entry.getEffects(player)) {
                if (currentInstinct > effectWrapper.maxInstinct) {
                    // Instinct is too high
                    continue;
                }
                
                InstinctEffect effect = effectWrapper.effect;
                
                // First check if the need should be activated
                InstinctEffectWrapper pastEffect = pastEffects.get(effect);
                if (pastEffect == null) {
                    // Do not activate if all needs in this instinct are being met
                    if (needsMet) {
                        continue checkEffect;
                    }
                    // Do not activate if one of the needs doesn't want it
                    for (InstinctNeedWrapper needWrapper : entry.getNeeds(player)) {
                        if (!needWrapper.getNeed(player).shouldActivateEffect(needWrapper.getState(player), effect)) {
                            continue checkEffect;
                        }
                    }
                }
                
                // The effect to be added/amplified
                InstinctEffectWrapper newEffect = newEffects.get(effect);
                if (newEffect == null) {
                    newEffect = new InstinctEffectWrapper(effectWrapper);
                    newEffects.put(effect, newEffect);
                }
                else {
                    // There are multiple effects of this type. Combine them.
                    newEffect.combine(effectWrapper);
                }
                
                if (pastEffect != null) {
                    // The effect already is applied. Keep its amplifier.
                    newEffect.combine(pastEffect);
                }
                
                Float newAmplifier = effectAmplifiers.get(effect);
                if (newAmplifier != null) {
                    // A need has requested for this effect to be amplified
                    newEffect.amplify(newAmplifier);
                    // The amplifier has been applied, so don't use it again.
                    effectAmplifiers.remove(effect);
                    for (InstinctNeedWrapper needWrapper : entry.getNeeds(player)) {
                        needWrapper.state.effectAmplifiers.remove(effect);
                    }
                }
            }
        }
        
        return newEffects;
    }
    
    /**
     * Helper function for differential instinct effect state (useful for efficient networking).
     * If an instinct effect is in the map, either it has been added or its amplifier has changed.
     * If the stored value is null, that effect has been deactivated.
     */
    public static Map<InstinctEffect, InstinctEffectWrapper> getEffectChanges(Map<InstinctEffect, InstinctEffectWrapper> oldEffects,
            Map<InstinctEffect, InstinctEffectWrapper> newEffects) {
        Map<InstinctEffect, InstinctEffectWrapper> changes = new HashMap<>();
        
        for (InstinctEffect oldEffect : oldEffects.keySet()) {
            changes.put(oldEffect, null);
        }
        for (InstinctEffectWrapper newEffectWrapper : newEffects.values()) {
            InstinctEffect effect = newEffectWrapper.effect;
            if (changes.get(effect) == null ||
                    oldEffects.get(effect).amplifier != newEffectWrapper.amplifier) {
                changes.put(effect, newEffectWrapper);
            }
        }
        
        return changes;
    }
    
    /**
     * Given a list of effects which may have changed, call activate/deactivate
     * when necessary and then update the list of active effects.
     * 
     * See ListenerPlayerInstinct.getEffectChanges
     */
    public static void transitionEffects(EntityPlayer player,
            ICapabilityInstinct instinct,
            Map<InstinctEffect, InstinctEffectWrapper> effectChanges) {
        Map<InstinctEffect, InstinctEffectWrapper> pastEffects = instinct.getActiveEffects();
        Map<InstinctEffect, InstinctEffectWrapper> newEffects = new HashMap<>();
        newEffects.putAll(pastEffects);
        
        for (Map.Entry<InstinctEffect, InstinctEffectWrapper> changes : effectChanges.entrySet()) {
            InstinctEffect effect = changes.getKey();
            InstinctEffectWrapper wrapper = changes.getValue();
            InstinctEffectWrapper pastWrapper = pastEffects.get(effect);
            if (wrapper == null) {
                if (pastWrapper != null) {
                    pastWrapper.effect.onDeactivate(player, pastWrapper.amplifier);
                    newEffects.remove(effect);
                }
            }
            else {
                if (pastWrapper == null) {
                    effect.onActivate(player, wrapper.amplifier);
                    newEffects.put(effect, wrapper);
                }
                else {
                    if (pastWrapper.amplifier != wrapper.amplifier) {
                        effect.onDeactivate(player, pastWrapper.amplifier);
                        effect.onActivate(player, wrapper.amplifier);
                        newEffects.put(effect, wrapper);
                    }
                }
            }
        }
        
        // Finally, update active effects
        instinct.setActiveEffects(newEffects);
    }
    
    /**
     * Displays a random instinct need in chat.
     * If necessary, randomly iterate through the needs
     * until one of them gives a non-null need message.
     */
    public static void sendRandomInstinctToChat(EntityPlayerMP player, ICapabilityInstinct instinctCap) {
        List<InstinctNeedWrapper> allNeeds = new ArrayList<>();
        for (ICapabilityInstinct.InstinctEntry entry : instinctCap.getInstincts()) {
            allNeeds.addAll(entry.getNeeds(player));
        }
        Collections.shuffle(allNeeds);
        
        for (InstinctNeedWrapper need : allNeeds) {
            ITextComponent needMessage = need.getNeed(player).getNeedMessage(need.state.needStatus);
            if (needMessage != null) {
                Chat.message(Chat.Type.NOTIFY, player, needMessage);
                break;
            }
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
