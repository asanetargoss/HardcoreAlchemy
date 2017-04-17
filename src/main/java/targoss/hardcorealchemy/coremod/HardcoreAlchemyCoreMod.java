package targoss.hardcorealchemy.coremod;

import java.util.Arrays;
import java.util.Map;

import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import targoss.hardcorealchemy.coremod.transform.TUseless;

@IFMLLoadingPlugin.Name(value = "Hardcore Alchemy Core")
@IFMLLoadingPlugin.MCVersion(value = "1.10.2")
@IFMLLoadingPlugin.TransformerExclusions(value = "targoss.hardcorealchemy.coremod.")
@IFMLLoadingPlugin.SortingIndex(value = 1001)
public class HardcoreAlchemyCoreMod implements IFMLLoadingPlugin {

	@Override
	public String[] getASMTransformerClass() {
		return new String[]{
				TUseless.class.getName()
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
			meta.modId = "targoss-hardcorealchemy-coremod";
			meta.name = "Hardcore Alchemy Core Mod";
			meta.version = "0.1.0";
			meta.credits = "";
			meta.authorList = Arrays.asList("asanetargoss");
			meta.description = "The secret sauce.";
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getAccessTransformerClass() {
		// TODO Auto-generated method stub
		return null;
	}

}
