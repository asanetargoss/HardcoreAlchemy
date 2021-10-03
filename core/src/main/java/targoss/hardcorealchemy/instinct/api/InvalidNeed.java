/*
 * Copyright 2019 asanetargoss
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

package targoss.hardcorealchemy.instinct.api;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import targoss.hardcorealchemy.instinct.api.IInstinctState.NeedStatus;

public class InvalidNeed implements IInstinctNeed {
    
    private InvalidNeed() {}
    
    public static final InvalidNeed INSTANCE = new InvalidNeed();

    @Override
    public NBTTagCompound serializeNBT() {
        return new NBTTagCompound();
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) { }

    @Override
    public IInstinctNeed createInstanceFromMorphEntity(EntityLivingBase morphEntity) {
        return null;
    }

    @Override
    public @Nullable ITextComponent getNeedMessage(NeedStatus needStatus) {
        return null;
    }

    @Override
    public @Nullable ITextComponent getNeedUnfulfilledMessage(NeedStatus needStatus) {
        return null;
    }

    @Override
    public void tick(IInstinctState instinctState) { }

}
