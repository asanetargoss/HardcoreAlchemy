package targoss.hardcorealchemy.coremod;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.TraceClassVisitor;

import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import targoss.hardcorealchemy.coremod.transform.TEntityLivingBase;
import targoss.hardcorealchemy.coremod.transform.TProjectEKeyHandler;
import targoss.hardcorealchemy.coremod.transform.TSlot;

@IFMLLoadingPlugin.Name(value = "Hardcore Alchemy Core")
@IFMLLoadingPlugin.MCVersion(value = "1.10.2")
@IFMLLoadingPlugin.TransformerExclusions(value = "targoss.hardcorealchemy.coremod.")
@IFMLLoadingPlugin.SortingIndex(value = 1001)
public class HardcoreAlchemyCoreMod implements IFMLLoadingPlugin {
    
    public static boolean obfuscated = false;
    
    public static final Logger LOGGER = LogManager.getLogger("Hardcore Alchemy Coremod");
    
	@Override
	public String[] getASMTransformerClass() {
		return new String[]{
				TEntityLivingBase.class.getName(),
				TSlot.class.getName(),
				TProjectEKeyHandler.class.getName()
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
			meta.modId = "hardcorealchemy-coremod";
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
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) {
		obfuscated = (Boolean)(data.get("runtimeDeobfuscationEnabled"));
		LOGGER.debug("runtimeDeobfuscationEnabled: "+obfuscated);
	}

	@Override
	public String getAccessTransformerClass() {
		return null;
	}
	
	public static void logBytesToDebug(byte[] bytes) {
	    StringWriter stringWriter = new StringWriter();
        TraceClassVisitor traceVisitor = new TraceClassVisitor(new PrintWriter(stringWriter));
        (new ClassReader(bytes)).accept(traceVisitor, 0);
        LOGGER.debug(stringWriter.getBuffer());
	}

}
