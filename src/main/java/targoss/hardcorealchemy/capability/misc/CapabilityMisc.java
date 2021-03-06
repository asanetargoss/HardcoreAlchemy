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

package targoss.hardcorealchemy.capability.misc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.modpack.guide.HCAUpgradeGuides;

public class CapabilityMisc implements ICapabilityMisc {
    public static final ResourceLocation RESOURCE_LOCATION = new ResourceLocation(HardcoreAlchemy.MOD_ID, "misc");
    
    protected UUID lifetimeUUID = null;
    protected int lastIncantationTick = 0;
    protected boolean hasChangedDimensionWhileAlive = false;
    protected String lastLoginVersion = HCAUpgradeGuides.UPGRADE_GUIDES.getDefaultExpectedPlayerVersion();
    protected List<ItemStack> pendingInventoryGifts = new ArrayList<>();

    @Override
    public UUID getLifetimeUUID() {
        return lifetimeUUID;
    }

    @Override
    public void setLifetimeUUID(UUID uuid) {
        this.lifetimeUUID = uuid;
    }

    @Override
    public int getLastIncantationTick() {
        return this.lastIncantationTick;
    }

    @Override
    public void setLastIncantationTick(int lastIncantationTick) {
        this.lastIncantationTick = lastIncantationTick;
    }

    @Override
    public boolean getHasChangedDimensionWhileAlive() {
        return this.hasChangedDimensionWhileAlive;
    }

    @Override
    public void setHasChangedDimensionWhileAlive(boolean hasChangedDimensionWhileAlive) {
        this.hasChangedDimensionWhileAlive = hasChangedDimensionWhileAlive;
    }

    @Override
    public String getLastLoginVersion() {
        return lastLoginVersion;
    }

    @Override
    public void setLastLoginVersion(String lastLoginVersion) {
        this.lastLoginVersion = lastLoginVersion;
    }

    @Override
    public List<ItemStack> getPendingInventoryGifts() {
        return pendingInventoryGifts;
    }

    @Override
    public void setPendingInventoryGifts(List<ItemStack> pendingInventoryGifts) {
        this.pendingInventoryGifts = pendingInventoryGifts;
    }
}
