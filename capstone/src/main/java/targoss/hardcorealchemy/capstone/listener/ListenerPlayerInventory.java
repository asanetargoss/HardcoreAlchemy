/*
 * Copyright 2017-2022 asanetargoss
 *
 * This file is part of Hardcore Alchemy Capstone.
 *
 * Hardcore Alchemy Capstone is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * Hardcore Alchemy Capstone is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Hardcore Alchemy Capstone.  If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.capstone.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import targoss.hardcorealchemy.capability.misc.ICapabilityMisc;
import targoss.hardcorealchemy.capability.misc.ProviderMisc;
import targoss.hardcorealchemy.capstone.HardcoreAlchemyCapstone;
import targoss.hardcorealchemy.capstone.guide.HCAUpgradeGuides;
import targoss.hardcorealchemy.listener.HardcoreAlchemyListener;

public class ListenerPlayerInventory extends HardcoreAlchemyListener {
    protected Random random = new Random();
    
    @SubscribeEvent
    public void onPlayerNeedUpgradeGuides(PlayerLoggedInEvent event) {
        ICapabilityMisc misc = event.player.getCapability(ProviderMisc.MISC_CAPABILITY, null);
        if (misc == null) {
            return;
        }
        String lastLoginVersion = misc.getLastLoginVersion();
        List<ItemStack> upgradeGuides = HCAUpgradeGuides.UPGRADE_GUIDES.getUpgradeGuidesSinceVersion(lastLoginVersion);
        List<ItemStack> gifts = new ArrayList<>();
        gifts.addAll(misc.getPendingInventoryGifts());
        gifts.addAll(upgradeGuides);
        misc.setPendingInventoryGifts(gifts);
    }
    
    protected void addGifts(EntityPlayer player, ICapabilityMisc misc) {
        List<ItemStack> pendingGifts = misc.getPendingInventoryGifts();
        int numGifts = pendingGifts.size();
        int nextGift = 0;
        for (; nextGift < numGifts; ++nextGift) {
            if (!player.inventory.addItemStackToInventory(pendingGifts.get(nextGift))) {
                break;
            }
        }
        List<ItemStack> newPendingGifts = new ArrayList<>(numGifts - nextGift);
        for (int i = nextGift; i < numGifts; ++i) {
            newPendingGifts.add(pendingGifts.get(i));
        }
        misc.setPendingInventoryGifts(newPendingGifts);
        if (nextGift != 0) {
            player.world.playSound((EntityPlayer)null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F, ((random.nextFloat() - random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
            if (nextGift == pendingGifts.size()) {
                misc.setLastLoginVersion(HardcoreAlchemyCapstone.VERSION);
            }
        }
    }
    
    @SubscribeEvent
    public void onPlayerGetGifts(PlayerTickEvent event) {
        if (event.phase != Phase.END) {
            return;
        }
        if (event.player.world.isRemote) {
            return;
        }
        ICapabilityMisc misc = event.player.getCapability(ProviderMisc.MISC_CAPABILITY, null);
        if (misc == null) {
            return;
        }
        if (misc.getPendingInventoryGifts().size() == 0) {
            return;
        }
        
        addGifts(event.player, misc);
    }

}
