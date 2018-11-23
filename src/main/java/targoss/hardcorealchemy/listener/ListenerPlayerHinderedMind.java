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

import java.util.HashMap;
import java.util.Map;

import mchorse.metamorph.api.MorphManager;
import mchorse.metamorph.api.abilities.IAbility;
import mchorse.metamorph.api.morphs.AbstractMorph;
import mchorse.metamorph.capabilities.morphing.IMorphing;
import mchorse.metamorph.capabilities.morphing.Morphing;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.util.MovementInput;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import targoss.hardcorealchemy.capability.humanity.ICapabilityHumanity;
import targoss.hardcorealchemy.capability.humanity.ProviderHumanity;
import targoss.hardcorealchemy.config.Configs;
import targoss.hardcorealchemy.coremod.CoremodHook;
import targoss.hardcorealchemy.event.EventDrawInventoryItem;
import targoss.hardcorealchemy.event.EventDrawWorldItem;
import targoss.hardcorealchemy.event.EventRenderSlotTooltip;
import targoss.hardcorealchemy.event.EventTakeStack;
import targoss.hardcorealchemy.util.MiscVanilla;

public class ListenerPlayerHinderedMind extends ConfiguredListener {
    public ListenerPlayerHinderedMind(Configs configs) {
        super(configs);
    }
    
    private boolean heldItemTooltips = true;
    private boolean renderHotbar = true;
    private boolean renderHealth = true;
    private boolean renderArmor = true;
    private boolean renderFood = true;
    private boolean renderHealthMount = true;
    private boolean renderAir = true;
    private boolean renderExperience = true;
    private boolean renderJumpBar = true;
    
    private Map<Item, Item> itemObfuscation = new HashMap<>();
    
    @Override
    public void serverAboutToStart(FMLServerAboutToStartEvent event) {
        heldItemTooltips = MiscVanilla.getHeldItemTooltips();

        Item coal = Items.COAL;
        itemObfuscation.put(coal, coal);
        itemObfuscation.put(Items.REDSTONE, coal);
        itemObfuscation.put(Items.DIAMOND, coal);
        itemObfuscation.put(Items.IRON_INGOT, coal);
        itemObfuscation.put(Items.GOLD_INGOT, coal);
        itemObfuscation.put(Items.DYE, coal);
        itemObfuscation.put(Items.FLINT, coal);
        itemObfuscation.put(Items.GLOWSTONE_DUST, coal);
        itemObfuscation.put(Items.ENDER_PEARL, coal);
        itemObfuscation.put(Items.EMERALD, coal);
        itemObfuscation.put(Items.NETHER_STAR, coal);
    }
    
    private Item getObfuscatedItem(Item item) {
        if (itemObfuscation.containsKey(item)) {
            return itemObfuscation.get(item);
        }
        
        if (item instanceof ItemBlock) {
            return Item.getItemFromBlock(Blocks.STONE);
        }
        
        if (item instanceof ItemTool) {
            return Items.STONE_PICKAXE;
        }
        
        if (item instanceof ItemSword) {
            return Items.STONE_SWORD;
        }
        
        if (item instanceof ItemHoe) {
            return Items.STONE_HOE;
        }
        
        return null;
    }
    
    public static boolean isPlayerHindered(EntityPlayer player) {
        IMorphing morphing = Morphing.get(player);
        if (morphing == null) {
            return false;
        }
        
        if (morphing.getCurrentMorph() == null) {
            return false;
        }
        
        ICapabilityHumanity humanity = player.getCapability(ProviderHumanity.HUMANITY_CAPABILITY, null);
        if (humanity == null || humanity.canMorph()) {
            return false;
        }
        
        return true;
    }
    
    @CoremodHook
    public static boolean canPlayerUseSneakToPreventFall(Entity entity) {
        return (entity instanceof EntityPlayer) && !isPlayerHindered((EntityPlayer)entity);
    }
    
    @CoremodHook
    public static boolean isPlayerSneakingToPreventAutoJump(EntityPlayer player) {
        return player.isSneaking() && !isPlayerHindered(player);
    }
    
    /**
     * Check if this is a real inventory slot as opposed to some documentation/display slot
     */
    private static boolean isRealInventorySlot(Slot slot, EntityPlayer player) {
        // Special case
        if (slot == EventDrawInventoryItem.MOUSE_SLOT) {
            return true;
        }
        // The best way to check if an item is actually in a real inventory slot,
        // as opposed to some form of documentation or display, is to check
        // if the player can take the item.
        // However, we added an event which can stop this from happening
        // in a real inventory slot, so we need to check for that case
        // There is also a possible case where the event is canceled yet the
        // slot isn't real, which would cause this heuristic to fail.
        // For now, this is enough.
        EventTakeStack.Pre stackEvent = new EventTakeStack.Pre(slot, player);
        // Can take stack, or was taking stack canceled?
        return (slot.canTakeStack(player) || MinecraftForge.EVENT_BUS.post(stackEvent));
    }
    
    @SubscribeEvent
    public void onDrawInventoryItem(EventDrawInventoryItem event) {
        if (MiscVanilla.isEmptyItemStack(event.itemStack)) {
            return;
        }
        
        EntityPlayer player = MiscVanilla.getTheMinecraftPlayer();
        if (!isPlayerHindered(player)) {
            return;
        }
        
        if (!isRealInventorySlot(event.slot, player)) {
            return;
        }
        
        ItemStack itemStack = event.itemStack;
        Item item = itemStack.getItem();
        itemStack.stackSize = Math.min(4, itemStack.stackSize);
        if ((item instanceof ItemTool) ||
                (item instanceof ItemSword) ||
                (item instanceof ItemArmor) ||
                (item instanceof ItemShield) ||
                (item instanceof ItemBlock /* For essentia jars from Thaumcraft */)) {
            itemStack.setItemDamage(0);
        }

        Item oldItem = itemStack.getItem();
        Item obfuscatedItem = getObfuscatedItem(oldItem);
        if (obfuscatedItem != null) {
            // TODO: Set the item field directly because this initializes capabilities every time it is called, which is really awful
            itemStack.setItem(obfuscatedItem);
            if (!itemStack.isItemStackDamageable()) {
                itemStack.setItemDamage(0);
            }
        }
    }
    
    @SubscribeEvent
    public void onDrawWorldItem(EventDrawWorldItem event) {
        if (!isPlayerHindered(MiscVanilla.getTheMinecraftPlayer())) {
            return;
        }
        
        if (MiscVanilla.isEmptyItemStack(event.itemStack)) {
            return;
        }
        
        ItemStack itemStack = event.itemStack;
        Item oldItem = itemStack.getItem();
        Item obfuscatedItem = getObfuscatedItem(oldItem);
        if (obfuscatedItem != null) {
            // TODO: Set the item field directly because this initializes capabilities every time it is called, which is really awful
            itemStack.setItem(obfuscatedItem);
            if (!itemStack.isItemStackDamageable()) {
                itemStack.setItemDamage(0);
            }
        }
    }
    
    @SubscribeEvent
    public void onDisplayTooltip(EventRenderSlotTooltip.Pre event) {
        EntityPlayer player = MiscVanilla.getTheMinecraftPlayer();
        
        if (!isPlayerHindered(player)) {
            return;
        }
        
        if (!isRealInventorySlot(event.slot, player)) {
            return;
        }
        
        event.setCanceled(true);
    }
    
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public void onRenderOverlayPre(RenderGameOverlayEvent.Pre event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) {
            return;
        }
        if (!isPlayerHindered(MiscVanilla.getTheMinecraftPlayer())) {
            MiscVanilla.setHeldItemTooltips(heldItemTooltips);
            return;
        }
        
        MiscVanilla.setHeldItemTooltips(false);
        
        renderHotbar = GuiIngameForge.renderHotbar;
        renderHealth = GuiIngameForge.renderHealth;
        renderArmor = GuiIngameForge.renderArmor;
        renderFood = GuiIngameForge.renderFood;
        renderHealthMount = GuiIngameForge.renderHealthMount;
        renderAir = GuiIngameForge.renderAir;
        renderExperience = GuiIngameForge.renderExperiance;
        renderJumpBar = GuiIngameForge.renderJumpBar;

        GuiIngameForge.renderHotbar = false;
        GuiIngameForge.renderHealth = false;
        GuiIngameForge.renderArmor = false;
        GuiIngameForge.renderFood = false;
        GuiIngameForge.renderHealthMount = false;
        GuiIngameForge.renderAir = false;
        GuiIngameForge.renderExperiance = false;
        GuiIngameForge.renderJumpBar = false;
    }
    
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public void onRenderOverlayPost(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) {
            return;
        }

        GuiIngameForge.renderHotbar = renderHotbar;
        GuiIngameForge.renderHealth = renderHealth;
        GuiIngameForge.renderArmor = renderArmor;
        GuiIngameForge.renderFood = renderFood;
        GuiIngameForge.renderHealthMount = renderHealthMount;
        GuiIngameForge.renderAir = renderAir;
        GuiIngameForge.renderExperiance = renderExperience;
        GuiIngameForge.renderJumpBar = renderJumpBar;
    }
    
    public static class RiggedMovementInput extends MovementInput {
        private final MovementInput delegate;
        public RiggedMovementInput(MovementInput movementInput) {
            this.delegate = movementInput;
        }
        
        @Override
        public void updatePlayerMoveState() {
            delegate.updatePlayerMoveState();
            moveStrafe = delegate.moveStrafe;
            moveForward = delegate.moveForward;
            forwardKeyDown = delegate.forwardKeyDown;
            backKeyDown = delegate.backKeyDown;
            leftKeyDown = delegate.leftKeyDown;
            rightKeyDown = delegate.rightKeyDown;
            jump = delegate.jump;
            sneak = delegate.sneak;
            
            EntityPlayer player = MiscVanilla.getTheMinecraftPlayer();
            if (ListenerPlayerHinderedMind.isPlayerHindered(player)) {
                ((EntityPlayerSP)player).autoJumpEnabled = true;
                if (!player.capabilities.allowFlying) {
                    jump = false;
                }
                if (sneak) {
                    // Undo the movement bonus of sneaking
                    moveStrafe = moveStrafe / 0.3F;
                    moveForward = moveForward / 0.3F;
                }
                player.setSprinting(false);

                // Auto-swim if the player is in water and doesn't have the swim ability
                if ((player.isInWater() || player.isInLava()) && player.getRNG().nextFloat() < 0.8F) {
                    jump = true;
                    IMorphing morphing = Morphing.get(player);
                    if (morphing != null) {
                        // Check if this player has a swim ability of their own
                        AbstractMorph morph = morphing.getCurrentMorph();
                        if (morph != null) {
                            IAbility swim = MorphManager.INSTANCE.abilities.get("swim");
                            for (IAbility ability : morph.settings.abilities) {
                                if (ability == swim) {
                                    jump = false;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    private boolean isMovementRigged = false; 
    
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onLoadWorld(WorldEvent.Load event) {
        isMovementRigged = false;
    }
    
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onCheckClientMovement(ClientTickEvent event) {
        if (isMovementRigged) {
            return;
        }
        
        EntityPlayerSP player = (EntityPlayerSP)MiscVanilla.getTheMinecraftPlayer();
        if (player == null) {
            return;
        }
        
        player.movementInput = new RiggedMovementInput(player.movementInput);
        isMovementRigged = true;
    }
}
