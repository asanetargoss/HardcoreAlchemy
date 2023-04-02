/*
 * Copyright 2017-2023 asanetargoss
 *
 * This file is part of Hardcore Alchemy Tweaks.
 *
 * Hardcore Alchemy Tweaks is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Tweaks is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Tweaks. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.tweaks.listener;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.animation.ITimeValue;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;
import targoss.hardcorealchemy.capability.VirtualCapabilityManager;
import targoss.hardcorealchemy.capability.misc.ICapabilityMisc;
import targoss.hardcorealchemy.capability.misc.ProviderMisc;
import targoss.hardcorealchemy.listener.HardcoreAlchemyListener;
import targoss.hardcorealchemy.tweaks.HardcoreAlchemyTweaks;
import targoss.hardcorealchemy.tweaks.capability.itemcontainer.CapabilityItemContainer;
import targoss.hardcorealchemy.tweaks.capability.itemcontainer.ICapabilityItemContainer;
import targoss.hardcorealchemy.tweaks.capability.itemcontainer.ProviderItemContainer;
import targoss.hardcorealchemy.tweaks.capability.itemcontainer.StorageItemContainer;
import targoss.hardcorealchemy.tweaks.event.EventWorkbenchCraft;
import targoss.hardcorealchemy.tweaks.item.Items;
import targoss.hardcorealchemy.tweaks.network.RequestCraftItemTimefrozen;
import targoss.hardcorealchemy.util.InventoryUtil;

public class ListenerCraftTimefrozen extends HardcoreAlchemyListener {
    @Override
    public void registerCapabilities(CapabilityManager manager, VirtualCapabilityManager virtualManager) {
        manager.register(ICapabilityItemContainer.class, new StorageItemContainer(), CapabilityItemContainer.class);
        virtualManager.registerVirtualCapability(CapabilityItemContainer.RESOURCE_LOCATION, ProviderItemContainer.CAPABILITY_ITEM_CONTAINER);
    }


    @SubscribeEvent
    public void onItemCrafted(EventWorkbenchCraft event) {
        if (event.player.world.isRemote) {
            // Handled in phase 1
            return;
        }
        // Phase 2: Apply item model render state sent from client
        ICapabilityMisc misc = event.player.getCapability(ProviderMisc.MISC_CAPABILITY, null);
        if (misc == null) {
            return;
        }
        ItemStack itemStackToCraft = event.craftResult;
        if (InventoryUtil.isEmptyItemStack(itemStackToCraft)) {
            return;
        }
        Item itemToCraft = itemStackToCraft.getItem();
        if (itemToCraft != Items.TIMEFROZEN) {
            return;
        }
        ICapabilityItemContainer container = VirtualCapabilityManager.INSTANCE.getVirtualCapability(itemStackToCraft, ProviderItemContainer.CAPABILITY_ITEM_CONTAINER, false);
        if (container != null) {
            Map<ResourceLocation, Float> itemModelProperties = new HashMap<>();
            for (Map.Entry<String, Float> entry : misc.getEnqueuedItemModelProperties().entrySet()) {
                itemModelProperties.put(new ResourceLocation(entry.getKey()), entry.getValue());
            }
            container.setPropertyOverrides(itemModelProperties);
            
            // Now that the property overrides are set, store this virtual capability in the item NBT, so that it syncs to the client
            VirtualCapabilityManager.INSTANCE.updateVirtualCapability(itemStackToCraft, ProviderItemContainer.CAPABILITY_ITEM_CONTAINER);
            
            // When done, clear the properties in the capability so they aren't reused
            misc.getEnqueuedItemModelProperties().clear();
        }
    }
    
    public static class ClientSide extends HardcoreAlchemyListener {
        @SubscribeEvent
        public void onItemCrafted(ItemCraftedEvent event) {
            if (!event.player.world.isRemote) {
                // We can't handle phase 2 here as the server-side version of
                // this event is handled too late.
                return;
            }
            // Phase 1: Send item model render state to server
            ItemStack itemStackToCraft = event.crafting;
            if (InventoryUtil.isEmptyItemStack(itemStackToCraft)) {
                return;
            }
            Item itemToCraft = itemStackToCraft.getItem();
            if (itemToCraft != Items.TIMEFROZEN) {
                return;
            }
            ICapabilityItemContainer container = VirtualCapabilityManager.INSTANCE.getVirtualCapability(itemStackToCraft, ProviderItemContainer.CAPABILITY_ITEM_CONTAINER, false);
            if (container == null) {
                return;
            }
            ItemStack itemStackToFreeze = container.getContainedItem();
            if (InventoryUtil.isEmptyItemStack(itemStackToFreeze)) {
                return;
            }
            Item itemToFreeze = itemStackToFreeze.getItem();
            ImmutableMap<String, ITimeValue> animParams = itemToFreeze.getAnimationParameters(itemStackToFreeze, event.player.world, event.player);
            long time = event.player.world.getTotalWorldTime();
            Map<String, Float> frozenProperties = new HashMap<>();
            for (Map.Entry<String, ITimeValue> animParam : animParams.entrySet()) {
                float value = animParam.getValue().apply(time);
                frozenProperties.put(animParam.getKey(), value);
            }
            // Tested item, clock, appears to send additional properties; not sure if important: minecraft:lefthanded=0.0, minecraft:cooldown=0.0
            HardcoreAlchemyTweaks.proxy.messenger.sendToServer(new RequestCraftItemTimefrozen(frozenProperties));
        }
    }
}
