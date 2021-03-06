/*
 * Copyright 2021 asanetargoss
 * 
 * This file is part of the Hardcore Alchemy capstone mod.
 * 
 * The Hardcore Alchemy capstone mod is free software: you can redistribute
 * it and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 3 of the
 * License.
 * 
 * The Hardcore Alchemy capstone mod is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the Hardcore Alchemy capstone mod. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.listener;

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
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.capability.misc.ICapabilityMisc;
import targoss.hardcorealchemy.capability.misc.ProviderMisc;
import targoss.hardcorealchemy.config.Configs;
import targoss.hardcorealchemy.modpack.guide.HCAUpgradeGuides;

public class ListenerPlayerInventory extends ConfiguredListener {
    public ListenerPlayerInventory(Configs configs) {
        super(configs);
    }
    
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
                misc.setLastLoginVersion(HardcoreAlchemy.VERSION);
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
