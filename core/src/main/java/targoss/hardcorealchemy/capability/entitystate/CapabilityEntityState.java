/*
 * Copyright 2017-2022 asanetargoss
 *
 * This file is part of Hardcore Alchemy Core.
 *
 * Hardcore Alchemy Core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Core is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Core. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.capability.entitystate;

import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import targoss.hardcorealchemy.HardcoreAlchemyCore;

public class CapabilityEntityState implements ICapabilityEntityState {
    public static final ResourceLocation RESOURCE_LOCATION = new ResourceLocation(HardcoreAlchemyCore.MOD_ID, "entity_state");
    
    protected UUID targetPlayerID = null;
    protected EntityLivingBase lastAttackTarget = null;
    protected int age = 0;
    protected int lifetime = -1;
    protected boolean traveledDimensionally = false;
    protected int previousDimension = 0;

    @Override
    public UUID getTargetPlayerID() {
        return targetPlayerID;
    }

    @Override
    public void setTargetPlayerID(@Nullable UUID playerID) {
        this.targetPlayerID = playerID;
    }

    @Override
    public int getAge() {
        return age;
    }

    @Override
    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public int getLifetime() {
        return lifetime;
    }

    @Override
    public void setLifetime(int lifetime) {
        this.lifetime = lifetime;
    }

    @Override
    public boolean getTraveledDimensionally() {
        return traveledDimensionally;
    }

    @Override
    public void setTraveledDimensionally(boolean traveledDimensionally) {
        this.traveledDimensionally = traveledDimensionally;
    }

    @Override
    public int getPreviousDimension() {
        return previousDimension;
    }

    @Override
    public void setPreviousDimension(int previousDimension) {
        this.previousDimension = previousDimension;
    }
}
