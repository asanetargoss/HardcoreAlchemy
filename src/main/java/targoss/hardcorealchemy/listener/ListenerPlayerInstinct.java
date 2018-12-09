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
import java.util.Arrays;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
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
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import targoss.hardcorealchemy.capability.instincts.CapabilityInstinct;
import targoss.hardcorealchemy.capability.instincts.ICapabilityInstinct;
import targoss.hardcorealchemy.capability.instincts.ICapabilityInstinct.InstinctEntry;
import targoss.hardcorealchemy.capability.instincts.ProviderInstinct;
import targoss.hardcorealchemy.config.Configs;
import targoss.hardcorealchemy.instinct.IInstinct;
import targoss.hardcorealchemy.network.MessageInstinctActive;
import targoss.hardcorealchemy.network.MessageInstinctValue;
import targoss.hardcorealchemy.network.PacketHandler;
import targoss.hardcorealchemy.util.Chat;
import targoss.hardcorealchemy.util.InventoryUtil;

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
    /** How many ticks must pass before an instinct selection has some chance of occurring (currently 1 day) */
    public static final int INSTINCT_SELECTION_RATE = 24000;
    /** If an instinct check fails, the amount of ticks that must pass before another check can occur */
    public static final int INSTINCT_CHECK_INTERVAL = INSTINCT_SELECTION_RATE / 4;
    /** Chance of an instinct selection occurring per check */
    public static final double INSTINCT_SELECTION_CHANCE = 0.3;
    
    private Random random = new Random();

    @SubscribeEvent
    public void onAttachCapability(AttachCapabilitiesEvent<Entity> event) {
        if (!(event.getObject() instanceof EntityPlayer)) {
            return;
        }
        
        event.addCapability(CapabilityInstinct.RESOURCE_LOCATION, new ProviderInstinct());
        AbstractAttributeMap attributeMap = ((EntityPlayer)event.getObject()).getAttributeMap();
        if (attributeMap.getAttributeInstance(ICapabilityInstinct.MAX_INSTINCT) == null) {
            attributeMap.registerAttribute(ICapabilityInstinct.MAX_INSTINCT);
        }
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
     * One instinct active at a time at most.
     * Instinct stat cannot increase/be restored while an instinct is active.
     * While there are no active instincts, all instincts get inactive-ticked,
     * with the potential to cause an increase/decrease in instinct. If no
     * inactively ticked instincts increment the instinct stat, then the instinct
     * stat is decreased by a constant amount.
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
        
        IInstinct activeInstinct = instinct.getActiveInstinct();
        if (activeInstinct != null) {
            if (activeInstinct.shouldStayActive(player)) {
                // Active instinct behavior
                activeInstinct.tick(player);
            }
            else {
                activeInstinct.onDeactivate(player);
                activeInstinct = null;
                instinct.setActiveInstinctId(null);
            }
        }
        else {
            // Instinct calculations when no instinct behavior is active
            // There will be a default instinct decrease unless one inactive instinct causes an increase
            boolean doDefaultInstinctDecrease = true;
            float instinctChange = 0.0F;
            
            for (ICapabilityInstinct.InstinctEntry entry : instinct.getInstinctMap().values()) {
                float newInstinctChange = entry.instinct.getInactiveChangeOnTick(player);
                doDefaultInstinctDecrease = (newInstinctChange <= 0) && doDefaultInstinctDecrease;
                instinctChange += newInstinctChange;
            }
            
            if (doDefaultInstinctDecrease) {
                instinctChange -= INSTINCT_LOSS_RATE;
            }
            
            // Apply instinct stat calculations
            addInstinct(player, instinct, instinctChange);
            
            // Select an active instinct if enough time has passed
            // Due to the randomness, the random check must occur server-side and be synced to the client
            if (!player.world.isRemote) {
                int inactiveInstinctTime = instinct.getInactiveInstinctTime();
                if (inactiveInstinctTime >= INSTINCT_SELECTION_RATE) {
                    // Select an active instinct if the random check succeeds and an instinct can be activated
                    ICapabilityInstinct.InstinctEntry newActiveInstinct = selectRandomInstinct(instinct, false);
                    
                    if (newActiveInstinct == null) {
                        // No instinct is available at all which matches
                        // the criteria. Reset timer so we don't check too often.
                        inactiveInstinctTime = 0;
                        // Display a message about the effects of a random instinct.
                        ICapabilityInstinct.InstinctEntry messageInstinct = selectRandomInstinct(instinct, true);
                        if (messageInstinct != null) {
                            ITextComponent needMessage = messageInstinct.instinct.getNeedMessage(player);
                            if (needMessage != null) {
                                Chat.message(Chat.Type.NOTIFY, (EntityPlayerMP)player, needMessage);
                            }
                        }
                    }
                    else if (random.nextFloat() < INSTINCT_SELECTION_CHANCE) {
                        // The player has become inflicted by a new instinct behavior!
                        instinct.setActiveInstinctId(newActiveInstinct.id);
                        ITextComponent needMessage = newActiveInstinct.instinct.getNeedMessageOnActivate(player);
                        if (needMessage != null) {
                            Chat.message(Chat.Type.WARN, (EntityPlayerMP)player, newActiveInstinct.instinct.getNeedMessageOnActivate(player));
                        }
                        newActiveInstinct.instinct.onActivate(player);
                        // Tell the client about the newly activated instinct.
                        // onActivate(player) will be called on the client's side when the packet is received.
                        PacketHandler.INSTANCE.sendTo(new MessageInstinctActive(instinct), (EntityPlayerMP)player);
                        inactiveInstinctTime = 0;
                    }
                    else {
                        // An instinct is available but the random check failed. Try again soon.
                        inactiveInstinctTime -= INSTINCT_CHECK_INTERVAL;
                        // Display a message about the effects of a random instinct.
                        ICapabilityInstinct.InstinctEntry messageInstinct = selectRandomInstinct(instinct, true);
                        if (messageInstinct != null) {
                            ITextComponent needMessage = messageInstinct.instinct.getNeedMessage(player);
                            if (needMessage != null) {
                                Chat.message(Chat.Type.NOTIFY, (EntityPlayerMP)player, needMessage);
                            }
                        }
                    }
                }
                else {
                    inactiveInstinctTime++;
                }
                instinct.setInactiveInstinctTime(inactiveInstinctTime);
            }
        }
    }
    
    /**
     * Selects random instinct based on weight.
     * anyInstinct = false means only select from instincts
     * which can be activated.
     */
    public @Nullable InstinctEntry selectRandomInstinct(ICapabilityInstinct instinct, boolean anyInstinct) {
        ArrayList<InstinctEntry> availableInstincts = new ArrayList<>();
        for (InstinctEntry entry : instinct.getInstinctMap().values()) {
            if (entry.weight > 0.0F && (anyInstinct || entry.instinct.getMaxAllowedInstinct() >= instinct.getInstinct())) {
                availableInstincts.add(entry);
            }
        }
        
        int n = availableInstincts.size();
        if (n <= 0) {
            return null;
        }
        
        /*
         * Randomly select an available instinct based on their weights.
         * 
         * Visualization/example:
         * 
         * random.nextFloat()*cumulativeWeight
         * ----------------------------------------------->
         * 
         * availableInstincts.get(?).weight
         * 0.5F      |1.0F               |0.7F            |
         * 
         * weightMap[?]                                   |cumulativeWeight
         * 0.0F      |0.5F               |1.5F            |2.3F
         */
        float[] weightMap = new float[n];
        float cumulativeWeight = 0.0F;
        for (int i = 0; i < n; i++) {
            weightMap[i] = cumulativeWeight;
            cumulativeWeight += availableInstincts.get(i).weight;
        }
        int selectedInstinct = Arrays.binarySearch(weightMap, random.nextFloat()*cumulativeWeight);
        if (selectedInstinct < 0) {
            /* Not found, but the index of the next smaller
             * value is encoded in the result.
             */
            selectedInstinct = (-selectedInstinct) - 2;
        }
        
        return availableInstincts.get(selectedInstinct);
    }
    
    @SubscribeEvent
    public void onBlockInteract(PlayerInteractEvent.RightClickBlock event) {
        EntityPlayer player = event.getEntityPlayer();
        ICapabilityInstinct instinct = player.getCapability(INSTINCT_CAPABILITY, null);
        if (instinct == null) {
            return;
        }
        IInstinct activeInstinct = instinct.getActiveInstinct();
        if (activeInstinct == null) {
            return;
        }
        
        BlockPos blockPos = event.getPos();
        Block block = player.world.getBlockState(blockPos).getBlock();
        if (!activeInstinct.canInteract(player, blockPos, block)) {
            event.setUseBlock(Result.DENY);
        }
    }
    
    @SubscribeEvent
    public void onItemInteract(PlayerInteractEvent.RightClickItem event) {
        EntityPlayer player = event.getEntityPlayer();
        ICapabilityInstinct instinct = player.getCapability(INSTINCT_CAPABILITY, null);
        if (instinct == null) {
            return;
        }
        IInstinct activeInstinct = instinct.getActiveInstinct();
        if (activeInstinct == null) {
            return;
        }
        
        if (!activeInstinct.canInteract(player, event.getItemStack())) {
            event.setCanceled(true);
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
        IInstinct activeInstinct = instinct.getActiveInstinct();
        if (activeInstinct == null) {
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
        for (Entity entity : player.world.getEntitiesInAABBexcluding(player, aabb, ListenerPlayerInstinct::isFlammableLivingEntity)) {
            if (!activeInstinct.canAttack(player, (EntityLivingBase)entity)) {
                event.setUseItem(Result.DENY);
                break;
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
        IInstinct activeInstinct = instinct.getActiveInstinct();
        if (activeInstinct == null) {
            return;
        }
        
        if (!activeInstinct.canAttack(player, event.getEntityLiving())) {
            event.setCanceled(true);
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
        
        updateInstinctAfterKill((EntityPlayer)agressor, entity);
    }
    
    public void updateInstinctAfterKill(EntityPlayer player, EntityLivingBase entity) {
        if (player.world.isRemote) {
            // Just in case...
            return;
        }
        
        ICapabilityInstinct instinct = player.getCapability(INSTINCT_CAPABILITY, null);
        if (instinct == null) {
            return;
        }
        
        IInstinct activeInstinct = instinct.getActiveInstinct();
        if (activeInstinct != null) {
            activeInstinct.afterKill(player, entity);
        }
        else {
            float instinctChange = 0.0F;
            for (ICapabilityInstinct.InstinctEntry entry : instinct.getInstinctMap().values()) {
                instinctChange += entry.instinct.getInactiveChangeOnKill(player, entity);
            }
            addInstinct(player, instinct, instinctChange);
            PacketHandler.INSTANCE.sendTo(new MessageInstinctValue(instinct), (EntityPlayerMP)player);
        }
    }
    
    @SubscribeEvent
    public void onPlayerDig(LeftClickBlock event) {
        EntityPlayer player = event.getEntityPlayer();
        
        ICapabilityInstinct instinct = player.getCapability(INSTINCT_CAPABILITY, null);
        if (instinct == null) {
            return;
        }
        
        IInstinct activeInstinct = instinct.getActiveInstinct();
        if (activeInstinct == null) {
            return;
        }
        
        if (!activeInstinct.canInteract(player, event.getItemStack())) {
            event.setUseItem(Result.DENY);
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
        
        IInstinct activeInstinct = instinct.getActiveInstinct();
        if (activeInstinct == null) {
            return;
        }
        
        activeInstinct.afterBlockHarvest(player, event);
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
        
        IInstinct activeInstinct = instinct.getActiveInstinct();
        if (activeInstinct == null) {
            return;
        }
        
        activeInstinct.afterKillEntityForDrops(player, event);
    }
}
