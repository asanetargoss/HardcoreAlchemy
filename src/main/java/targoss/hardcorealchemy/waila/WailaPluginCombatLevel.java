package targoss.hardcorealchemy.waila;

import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.IWailaRegistrar;
import mcp.mobius.waila.api.WailaPlugin;
import net.minecraft.entity.EntityLiving;

@WailaPlugin
public class WailaPluginCombatLevel implements IWailaPlugin {

    @Override
    public void register(IWailaRegistrar registrar) {
        ProviderCombatLevel combatLevel = new ProviderCombatLevel();
        registrar.registerBodyProvider(combatLevel, combatLevel.getTargetClass());
        registrar.registerNBTProvider(combatLevel, combatLevel.getTargetClass());
    }

}
