package targoss.hardcorealchemy.magic.coremod;

import java.util.Arrays;
import java.util.Map;

import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import targoss.hardcorealchemy.magic.HardcoreAlchemyMagic;
import targoss.hardcorealchemy.magic.coremod.transform.TEntityExtension;
import targoss.hardcorealchemy.magic.coremod.transform.TProjectEKeyHandler;
import targoss.hardcorealchemy.magic.coremod.transform.TThaumcraftPlayerEvents;

@IFMLLoadingPlugin.Name(value = "Hardcore Alchemy Magic Coremod")
@IFMLLoadingPlugin.MCVersion(value = "1.10.2")
@IFMLLoadingPlugin.TransformerExclusions(value = "targoss.hardcorealchemy.magic.coremod.")
@IFMLLoadingPlugin.SortingIndex(value = 1001)
public class HardcoreAlchemyMagicCoremod implements IFMLLoadingPlugin {
    private static boolean coremodInitialized = false;
    
    public static boolean isCoremodInitialized() {
        return coremodInitialized;
    }

    @Override
    public String[] getASMTransformerClass() {
        coremodInitialized = true;
        return new String[]{
            TEntityExtension.class.getName(),
            TProjectEKeyHandler.class.getName(),
            TThaumcraftPlayerEvents.class.getName()
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
            meta.modId = "hardcorealchemy-magic-coremod";
            meta.name = "Hardcore Alchemy Magic Coremod";
            meta.version = HardcoreAlchemyMagic.VERSION;
            meta.credits = "";
            meta.authorList = Arrays.asList("asanetargoss");
            meta.description = "Cinnamon magic.";
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
