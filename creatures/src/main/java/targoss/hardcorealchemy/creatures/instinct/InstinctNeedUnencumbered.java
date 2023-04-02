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

package targoss.hardcorealchemy.creatures.instinct;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import targoss.hardcorealchemy.creatures.instinct.api.IInstinctNeed;
import targoss.hardcorealchemy.creatures.instinct.api.IInstinctState;
import targoss.hardcorealchemy.creatures.instinct.api.IInstinctState.NeedStatus;
import targoss.hardcorealchemy.util.InventoryUtil;

public class InstinctNeedUnencumbered implements IInstinctNeed {
    @Override
    public NBTTagCompound serializeNBT() {
        return new NBTTagCompound();
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {}

    @Override
    public IInstinctNeed createInstanceFromMorphEntity(EntityLivingBase morphEntity) {
        return new InstinctNeedUnencumbered();
    }

    @Override
    public ITextComponent getNeedMessage(NeedStatus needStatus) {
        return null;
    }

    @Override
    public ITextComponent getNeedUnfulfilledMessage(NeedStatus needStatus) {
        if (needStatus == NeedStatus.URGENT) {
            return new TextComponentTranslation("hardcorealchemy.instinct.unencumbered.need");
        } else {
            return null;
        }
    }
    
    protected static boolean isWearingArmor(EntityPlayer player) {
        int n = InventoryUtil.getArmorInventorySize(player);
        for (int i = 0; i < n; i++) {
            ItemStack armorStack = InventoryUtil.getArmorStackInSlot(player, i);
            if (!InventoryUtil.isEmptyItemStack(armorStack)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void tick(IInstinctState instinctState) {
        EntityPlayer player = instinctState.getPlayer();
        if (isWearingArmor(player)) {
            instinctState.setNeedStatus(NeedStatus.URGENT);
        } else {
            instinctState.setNeedStatus(NeedStatus.NONE);
        }
    }

}
