/*
 * Copyright 2017-2022 asanetargoss
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

package targoss.hardcorealchemy.tweaks.waila;

import java.util.List;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaEntityAccessor;
import mcp.mobius.waila.api.IWailaEntityProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import targoss.hardcorealchemy.HardcoreAlchemyCore;
import targoss.hardcorealchemy.capability.combatlevel.ICapabilityCombatLevel;

public class ProviderCombatLevel implements IWailaEntityProvider {
    
    public static final String NBT_LABEL_LEVEL = HardcoreAlchemyCore.MOD_ID + ":combat_level";
    public static final String NBT_LABEL_LEVEL_DEFINED = HardcoreAlchemyCore.MOD_ID + ":combat_level_defined";
    
    public static Class getTargetClass() {
        return EntityLiving.class;
    }
    
    @Override
    public Entity getWailaOverride(IWailaEntityAccessor accessor, IWailaConfigHandler config) {
        return null;
    }

    @Override
    public List<String> getWailaHead(Entity entity, List<String> currenttip, IWailaEntityAccessor accessor,
            IWailaConfigHandler config) {
        return currenttip;
    }

    @Override
    public List<String> getWailaBody(Entity entity, List<String> currenttip, IWailaEntityAccessor accessor,
            IWailaConfigHandler config) {
        NBTTagCompound nbtCompound = accessor.getNBTData();
        if (nbtCompound.getBoolean(NBT_LABEL_LEVEL_DEFINED)) {
            String levelDisplay =
                    new TextComponentTranslation("hardcorealchemy.level.display",
                    nbtCompound.getInteger(NBT_LABEL_LEVEL)
                    ).getFormattedText();
            currenttip.add(levelDisplay);
        }
        return currenttip;
    }

    @Override
    public List<String> getWailaTail(Entity entity, List<String> currenttip, IWailaEntityAccessor accessor,
            IWailaConfigHandler config) {
        return currenttip;
    }

    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, Entity ent, NBTTagCompound tag, World world) {
        ICapabilityCombatLevel combatLevel = ent.getCapability(
                targoss.hardcorealchemy.capability.combatlevel.ProviderCombatLevel.COMBAT_LEVEL_CAPABILITY,
                null);
        if (combatLevel != null) {
            tag.setInteger(NBT_LABEL_LEVEL, combatLevel.getValue());
            tag.setBoolean(NBT_LABEL_LEVEL_DEFINED, true);
        }
        return tag;
    }
}
