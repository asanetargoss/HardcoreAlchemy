/*
 * Copyright 2017-2023 asanetargoss
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
import targoss.hardcorealchemy.coremod.transform.TClassInheritanceMultiMap;
import targoss.hardcorealchemy.coremod.transform.TCraftingManager;
import targoss.hardcorealchemy.coremod.transform.TDataVillage;
import targoss.hardcorealchemy.coremod.transform.TEntityLivingBase;
import targoss.hardcorealchemy.coremod.transform.TEntityPlayerSP;
import targoss.hardcorealchemy.coremod.transform.TGuiContainer;
import targoss.hardcorealchemy.coremod.transform.TItemRenderer;
import targoss.hardcorealchemy.coremod.transform.TMinecraftServer;
import targoss.hardcorealchemy.coremod.transform.TRenderEntityItem;
import targoss.hardcorealchemy.coremod.transform.TRenderItem;
import targoss.hardcorealchemy.coremod.transform.TSlot;
import targoss.hardcorealchemy.coremod.transform.TThirstOverlayHandler;
import targoss.hardcorealchemy.coremod.transform.TWorld;

@IFMLLoadingPlugin.Name(value = "Hardcore Alchemy Core Coremod")
@IFMLLoadingPlugin.MCVersion(value = "1.10.2")
@IFMLLoadingPlugin.TransformerExclusions(value = "targoss.hardcorealchemy.coremod.")
@IFMLLoadingPlugin.SortingIndex(value = 1001)
public class HardcoreAlchemyCoreCoremod implements IFMLLoadingPlugin {
    private static boolean coremodInitialized = false;
    
    public static boolean isCoremodInitialized() {
        return coremodInitialized;
    }
    
    public static boolean obfuscated = false;
    public static boolean TAN_LOADED = false;
    
    public static final Logger LOGGER = LogManager.getLogger("Hardcore Alchemy Core Coremod");
    
	@Override
	public String[] getASMTransformerClass() {
	    coremodInitialized = true;
		return new String[]{
	            TEntityLivingBase.class.getName(),
				TSlot.class.getName(),
				TCraftingManager.class.getName(),
				TThirstOverlayHandler.class.getName(),
				TClassInheritanceMultiMap.class.getName(),
				TDataVillage.class.getName(),
				TGuiContainer.class.getName(),
				TItemRenderer.class.getName(),
				TRenderEntityItem.class.getName(),
				TRenderItem.class.getName(),
				TEntityPlayerSP.class.getName(),
				TMinecraftServer.class.getName(),
				TWorld.class.getName()
		};
	}
	
	public static boolean enableDebugFeatures() {
	    return !obfuscated;
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
			meta.name = "Hardcore Alchemy Core Coremod";
			meta.version = targoss.hardcorealchemy.HardcoreAlchemyCore.VERSION;
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
