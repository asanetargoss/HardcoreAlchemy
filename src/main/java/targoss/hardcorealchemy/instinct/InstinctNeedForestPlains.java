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

package targoss.hardcorealchemy.instinct;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import targoss.hardcorealchemy.instinct.api.IInstinctNeed;
import targoss.hardcorealchemy.instinct.api.IInstinctState.NeedStatus;

public class InstinctNeedForestPlains extends InstinctNeedSpawnEnvironment {
    public InstinctNeedForestPlains(EntityLivingBase morphEntity) {
        super(morphEntity);
    }

    @Override
    public IInstinctNeed createInstanceFromMorphEntity(EntityLivingBase morphEntity) {
        return new InstinctNeedForestPlains(morphEntity);
    }
    
    @Override
    public ITextComponent getFeelsAtHomeMessage(NeedStatus needStatus) {
        return new TextComponentTranslation("hardcorealchemy.instinct.home.nature.fulfilled");
    }

    @Override
    public ITextComponent getNearHomeMessage() {
        return new TextComponentTranslation("hardcorealchemy.instinct.home.nature.need_nearby");
    }

    @Override
    public ITextComponent getFarFromHomeMessage() {
        return new TextComponentTranslation("hardcorealchemy.instinct.home.nature.need");
    }

}
