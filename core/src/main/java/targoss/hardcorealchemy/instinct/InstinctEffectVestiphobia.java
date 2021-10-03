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

package targoss.hardcorealchemy.instinct;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;
import targoss.hardcorealchemy.instinct.api.InstinctEffect;
import targoss.hardcorealchemy.util.Chat;
import targoss.hardcorealchemy.util.InventoryUtil;

public class InstinctEffectVestiphobia extends InstinctEffect {
    @Override
    public void onActivate(EntityPlayer player, float amplifier) {}

    @Override
    public void onDeactivate(EntityPlayer player, float amplifier) {}

    @Override
    public void tick(EntityPlayer player, float amplifier) {
        if (player.world.isRemote) {
            return;
        }
        if (amplifier < 2.0f) {
            return;
        }
        
        boolean droppedArmor = false;
        int n = InventoryUtil.getArmorInventorySize(player);
        for (int i = 0; i < n; i++) {
            ItemStack armorStack = InventoryUtil.getArmorStackInSlot(player, i);
            if (!InventoryUtil.isEmptyItemStack(armorStack)) {
                
                InventoryUtil.setArmorStackInSlot(player, i, InventoryUtil.ITEM_STACK_EMPTY);
                player.dropItem(armorStack, true);
                droppedArmor = true;
            }
        }
        if (droppedArmor && (player instanceof EntityPlayerMP)) {
            // TODO: i18n
            Chat.message(Chat.Type.NOTIFY, (EntityPlayerMP)player, new TextComponentTranslation("hardcorealchemy.instinct.unencumbered.action"));
        }
    }

}
