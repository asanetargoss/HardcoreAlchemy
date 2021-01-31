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

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import targoss.hardcorealchemy.instinct.api.IInstinctNeed;
import targoss.hardcorealchemy.instinct.api.IInstinctState;
import targoss.hardcorealchemy.instinct.api.IInstinctState.NeedStatus;
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
