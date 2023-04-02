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

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mchorse.metamorph.api.MorphManager;
import mchorse.metamorph.api.abilities.IAbility;
import mchorse.metamorph.api.morphs.AbstractMorph;
import mchorse.metamorph.capabilities.morphing.IMorphing;
import mchorse.metamorph.capabilities.morphing.Morphing;
import mcp.mobius.waila.api.event.WailaRenderEvent;
import mezz.jei.JustEnoughItems;
import mezz.jei.config.OverlayToggleEvent;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.input.IShowsRecipeFocuses;
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
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import targoss.hardcorealchemy.ModState;
import targoss.hardcorealchemy.coremod.CoremodHook;
import targoss.hardcorealchemy.creatures.capability.instinct.ICapabilityInstinct;
import targoss.hardcorealchemy.creatures.capability.instinct.ProviderInstinct;
import targoss.hardcorealchemy.creatures.instinct.Instincts;
import targoss.hardcorealchemy.creatures.instinct.internal.InstinctEffectWrapper;
import targoss.hardcorealchemy.event.EventDrawInventoryItem;
import targoss.hardcorealchemy.event.EventDrawWorldItem;
import targoss.hardcorealchemy.event.EventRenderSlotTooltip;
import targoss.hardcorealchemy.listener.HardcoreAlchemyListener;
import targoss.hardcorealchemy.util.InventoryUtil;
import targoss.hardcorealchemy.util.MiscVanilla;

public class ListenerPlayerHinderedMind extends HardcoreAlchemyListener {
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
        if (!event.getServer().isDedicatedServer()) {
            heldItemTooltips = MiscVanilla.getHeldItemTooltips();
        }

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
    
    public static float getPlayerHindrance(EntityPlayer player) {
        ICapabilityInstinct instinct = player.getCapability(ProviderInstinct.INSTINCT_CAPABILITY, null);
        if (instinct == null) {
            return 0.0F;
        }
        
        InstinctEffectWrapper wrapper = instinct.getActiveEffects().get(Instincts.EFFECT_HINDERED_MIND);
        if (wrapper == null) {
            return 0.0F;
        }
        return wrapper.amplifier;
    }
    
    public static float getPlayerFear(EntityPlayer player) {
        ICapabilityInstinct instinct = player.getCapability(ProviderInstinct.INSTINCT_CAPABILITY, null);
        if (instinct == null) {
            return 0.0F;
        }
        
        InstinctEffectWrapper wrapper = instinct.getActiveEffects().get(Instincts.EFFECT_HUNTED);
        if (wrapper == null) {
            return 0.0F;
        }
        return wrapper.amplifier;
    }
    
    @CoremodHook
    public static boolean canPlayerUseSneakToPreventFall(Entity entity) {
        return (entity instanceof EntityPlayer) && getPlayerHindrance((EntityPlayer)entity) < 1.0F;
    }
    
    @CoremodHook
    public static boolean isPlayerSneakingToPreventAutoJump(EntityPlayer player) {
        return player.isSneaking() && getPlayerHindrance(player) < 1.0F;
    }
    
    @SubscribeEvent
    public void onDrawInventoryItem(EventDrawInventoryItem event) {
        if (InventoryUtil.isEmptyItemStack(event.itemStack)) {
            return;
        }
        
        EntityPlayer player = MiscVanilla.getTheMinecraftPlayer();
        if (getPlayerHindrance(player) < 1.0F) {
            return;
        }
        
        if (!InventoryUtil.isInteractableSlot(event.slot, player)) {
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
        if (getPlayerHindrance(MiscVanilla.getTheMinecraftPlayer()) < 1.0F) {
            return;
        }
        
        if (InventoryUtil.isEmptyItemStack(event.itemStack)) {
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
        
        if (getPlayerHindrance(player) < 1.0F) {
            return;
        }
        
        if (!InventoryUtil.isInteractableSlot(event.slot, player)) {
            return;
        }
        
        event.setCanceled(true);
    }
    
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public void onRenderOverlayPre(RenderGameOverlayEvent.Pre event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) {
            return;
        }
        if (getPlayerHindrance(MiscVanilla.getTheMinecraftPlayer()) < 1.0F) {
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
            if (getPlayerHindrance(player) >= 1.0F) {
                //WORKAROUND: Auto-jump doesn't work well with Iron golems,
                // so only enable this for thinner players.
                //TODO: Improve auto-jump. ;)
                boolean canAutoJump = player.width < 1.0;
                if (canAutoJump) {
                    ((EntityPlayerSP)player).autoJumpEnabled = true;
                }
                if (!player.capabilities.allowFlying && canAutoJump) {
                    jump = false;
                }
                if (sneak) {
                    // Undo the movement bonus of sneaking
                    moveStrafe = moveStrafe / 0.3F;
                    moveForward = moveForward / 0.3F;
                }
                if (forwardKeyDown) {
                    player.setSprinting(getPlayerFear(player) >= 2.0);
                }

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

    @Optional.Method(modid=ModState.JEI_ID)
    private static List<IShowsRecipeFocuses> getJeiRecipeFocuses() {
        try {
            // Reflection... (JustEnoughItems.proxy.starter.guiEventHandler.inputHandler.showsRecipeFocuses)
            mezz.jei.ProxyCommonClient jeiProxy = (mezz.jei.ProxyCommonClient)JustEnoughItems.getProxy();
            Field jeiStarterField = jeiProxy.getClass().getDeclaredField("starter");
            jeiStarterField.setAccessible(true);
            mezz.jei.JeiStarter jeiStarter = (mezz.jei.JeiStarter)jeiStarterField.get(jeiProxy);
            Field guiEventHandlerField = jeiStarter.getClass().getDeclaredField("guiEventHandler");
            guiEventHandlerField.setAccessible(true);
            mezz.jei.GuiEventHandler guiEventHandler = (mezz.jei.GuiEventHandler)guiEventHandlerField.get(jeiStarter);
            Field inputHandlerField = guiEventHandler.getClass().getDeclaredField("inputHandler");
            inputHandlerField.setAccessible(true);
            mezz.jei.input.InputHandler inputHandler = (mezz.jei.input.InputHandler)inputHandlerField.get(guiEventHandler);
            if (inputHandler == null) {
                return null;
            }
            Field showsRecipeFocusesField = inputHandler.getClass().getDeclaredField("showsRecipeFocuses");
            showsRecipeFocusesField.setAccessible(true);
            return (List<IShowsRecipeFocuses>)showsRecipeFocusesField.get(inputHandler);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * When GuiContainerWrapper is replaced by this, and the player's mind is hindered,
     * JEI will no longer look up recipes on items in inventories.
     */
    @SideOnly(Side.CLIENT)
    public static class InventoryRecipeCheckStopper extends mezz.jei.input.GuiContainerWrapper {
        public mezz.jei.input.GuiContainerWrapper delegate;
        
        public InventoryRecipeCheckStopper (mezz.jei.input.GuiContainerWrapper delegate) {
            this.delegate = delegate;
        }

        @Override
        public IClickedIngredient<?> getIngredientUnderMouse(int mouseX, int mouseY) {
            Slot hoveredSlot = InventoryUtil.getSlotUnderMouse();
            EntityPlayer player = MiscVanilla.getTheMinecraftPlayer();
            if (hoveredSlot != null && InventoryUtil.isInteractableSlot(hoveredSlot, player) && getPlayerHindrance(player) >= 1.0F) {
                return null;
            }
            return delegate.getIngredientUnderMouse(mouseX, mouseY);
        }
        
        public boolean canSetFocusWithMouse() {
            return delegate.canSetFocusWithMouse();
        }
    }

    @Optional.Method(modid=ModState.JEI_ID)
    @SideOnly(Side.CLIENT)
    private static void overrideJEIInventoryHandler() {
        // This is all client side. Yay!
        List<IShowsRecipeFocuses> recipeShowers = getJeiRecipeFocuses();
        if (recipeShowers == null) {
            return;
        }
        int n = recipeShowers.size();
        for (int i = 0; i < n; i++) {
            IShowsRecipeFocuses recipeShower = recipeShowers.get(i);
            if (recipeShower.getClass() != mezz.jei.input.GuiContainerWrapper.class) {
                // Make sure we don't create an infinite chain of GuiContainerWrappers, in the case where JEI decides to refresh inventory handlers ad infinitum
                continue;
            }
            recipeShowers.set(i, new InventoryRecipeCheckStopper((mezz.jei.input.GuiContainerWrapper)recipeShower));
        }
    }
    
    @SubscribeEvent(priority=EventPriority.LOW)
    @Optional.Method(modid=ModState.JEI_ID)
    @SideOnly(Side.CLIENT)
    public void onGuiInitForJEI(GuiScreenEvent.InitGuiEvent.Post event) {
        overrideJEIInventoryHandler();
    }
    
    @SubscribeEvent(priority=EventPriority.LOW)
    @Optional.Method(modid=ModState.JEI_ID)
    @SideOnly(Side.CLIENT)
    public void onOverlayToggleForJEI(OverlayToggleEvent event) {
        overrideJEIInventoryHandler();
    }
    
    @SubscribeEvent(priority=EventPriority.LOW)
    @Optional.Method(modid=ModState.JEI_ID)
    @SideOnly(Side.CLIENT)
    public void onDrawBackgroundEventPostForJEI(GuiScreenEvent.BackgroundDrawnEvent event) {
        overrideJEIInventoryHandler();
    }
    
    @SubscribeEvent
    @Optional.Method(modid=ModState.WAWLA_ID)
    @SideOnly(Side.CLIENT)
    public void onRenderWAWLATooltip(WailaRenderEvent.Pre event) {
        if (getPlayerHindrance(MiscVanilla.getTheMinecraftPlayer()) >= 1.0F) {
            event.setCanceled(true);
        }
    }
}
