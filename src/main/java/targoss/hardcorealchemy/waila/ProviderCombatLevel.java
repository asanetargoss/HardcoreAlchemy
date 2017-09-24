package targoss.hardcorealchemy.waila;

import java.util.List;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaEntityAccessor;
import mcp.mobius.waila.api.IWailaEntityProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.capability.combatlevel.ICapabilityCombatLevel;

public class ProviderCombatLevel implements IWailaEntityProvider {
    
    public static final String NBT_LABEL_LEVEL = HardcoreAlchemy.MOD_ID + ":combat_level";
    public static final String NBT_LABEL_LEVEL_DEFINED = HardcoreAlchemy.MOD_ID + ":combat_level_defined";
    
    public static Class getTargetClass() {
        return EntityLiving.class;
    }
    
    @CapabilityInject(ICapabilityCombatLevel.class)
    public static final Capability<ICapabilityCombatLevel> COMBAT_LEVEL_CAPABILITY = null;
    
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
        ICapabilityCombatLevel combatLevel = ent.getCapability(COMBAT_LEVEL_CAPABILITY, null);
        if (combatLevel != null) {
            tag.setInteger(NBT_LABEL_LEVEL, combatLevel.getValue());
            tag.setBoolean(NBT_LABEL_LEVEL_DEFINED, true);
        }
        return tag;
    }
}
