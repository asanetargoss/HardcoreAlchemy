package targoss.hardcorealchemy.tweaks.coremod;

import java.util.Arrays;
import java.util.Map;

import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import targoss.hardcorealchemy.tweaks.HardcoreAlchemyTweaks;
import targoss.hardcorealchemy.tweaks.coremod.transform.TEntityLivingBase;
import targoss.hardcorealchemy.tweaks.coremod.transform.TInventoryPlayer;
import targoss.hardcorealchemy.tweaks.coremod.transform.TNetHandlerPlayServer;
import targoss.hardcorealchemy.tweaks.coremod.transform.TPlayerControllerMP;
import targoss.hardcorealchemy.tweaks.listener.TPlayerInteractionManager;

@IFMLLoadingPlugin.Name(value = "Hardcore Alchemy Tweaks Coremod")
@IFMLLoadingPlugin.MCVersion(value = "1.10.2")
@IFMLLoadingPlugin.TransformerExclusions(value = "targoss.hardcorealchemy.tweaks.coremod.")
@IFMLLoadingPlugin.SortingIndex(value = 1001)
public class HardcoreAlchemyTweaksCoremod implements IFMLLoadingPlugin {
    private static boolean coremodInitialized = false;
    
    public static boolean isCoremodInitialized() {
        return coremodInitialized;
    }

    @Override
    public String[] getASMTransformerClass() {
        coremodInitialized = true;
        return new String[]{
            TPlayerControllerMP.class.getName(),
            TInventoryPlayer.class.getName(),
            TEntityLivingBase.class.getName(),
            TNetHandlerPlayServer.class.getName(),
            TPlayerInteractionManager.class.getName()
        };
    }

    @Override
    public String getModContainerClass() {
        return Container.class.getName();
    }
    
    public static class Container extends DummyModContainer {
        public Container() {
            super(new ModMetadata());
            ModMetadata meta = getMetadata();
            meta.modId = "hardcorealchemy-tweaks-coremod";
            meta.name = "Hardcore Alchemy Tweaks Coremod";
            meta.version = HardcoreAlchemyTweaks.VERSION;
            meta.credits = "";
            meta.authorList = Arrays.asList("asanetargoss");
            meta.description = "Cinnamon tweaks.";
            meta.url = "";
            meta.screenshots = new String[0];
            meta.logoFile = "";
        }
        
        public boolean registerBus(EventBus bus, LoadController controller) {
            bus.register(this);
            return true;
        }
        
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) { }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

}
