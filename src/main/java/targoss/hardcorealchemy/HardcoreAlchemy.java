/**
 * Copyright 2017-2018 asanetargoss
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

package targoss.hardcorealchemy;

import java.util.Map;

import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import targoss.hardcorealchemy.command.CommandTest;
import targoss.hardcorealchemy.coremod.HardcoreAlchemyCoremod;
import targoss.hardcorealchemy.modpack.guide.HCAModpackGuide;
import targoss.hardcorealchemy.test.HardcoreAlchemyTests;

@Mod(modid = HardcoreAlchemy.MOD_ID, version = HardcoreAlchemy.VERSION,
    dependencies = HardcoreAlchemy.DEPENDENCIES, acceptedMinecraftVersions = HardcoreAlchemy.MC_VERSIONS)
public class HardcoreAlchemy
{
    public static final String MOD_ID = "hardcorealchemy";
    public static final String VERSION = "0.3.0";
    public static final String DEPENDENCIES = "required-after:metamorph;" +
            "after:astralsorcery;" +
            "after:adinferos;" +
            "after:uniquecrops;" +
            "after:" + ModState.GUIDEAPI_ID + ";" +
            "after:" + ModState.BLOOD_MAGIC_ID + ";" +
            "after:" + ModState.ARS_MAGICA_ID + ";" +
            "after:" + ModState.PROJECT_E_ID + ";" +
            "after:" + ModState.ARS_MAGICA_ID + ";" +
            "after:" + ModState.HARVESTCRAFT_ID + ";";
    public static final String MC_VERSIONS = "[1.10.2]";
    public static final String CLIENT_PROXY = "targoss.hardcorealchemy.ClientProxy";
    public static final String COMMON_PROXY = "targoss.hardcorealchemy.CommonProxy";
    
    @SidedProxy(modId=MOD_ID, clientSide=CLIENT_PROXY, serverSide=COMMON_PROXY)
    public static CommonProxy proxy;
    
    public static Logger LOGGER = null;
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        if (!HardcoreAlchemyCoremod.isCoremodInitialized()){
            throw new ModStateException(
                    "The coremod at '" +
                    HardcoreAlchemyCoremod.class.getName() +
                    "' did not run");
        }
        
        LOGGER = event.getModLog();
        
        Map<String, ModContainer> modMap = Loader.instance().getIndexedModList();
        ModState.isDissolutionLoaded = modMap.containsKey(ModState.DISSOLUTION_ID);
        ModState.isNutritionLoaded = modMap.containsKey(ModState.NUTRITION_ID);
        ModState.isBloodMagicLoaded = modMap.containsKey(ModState.BLOOD_MAGIC_ID);
        ModState.isArsMagicaLoaded = modMap.containsKey(ModState.ARS_MAGICA_ID);
        ModState.isProjectELoaded = modMap.containsKey(ModState.PROJECT_E_ID);
        ModState.isIronBackpacksLoaded = modMap.containsKey(ModState.IRON_BACKPACKS_ID);
        ModState.isTanLoaded = modMap.containsKey(ModState.TAN_ID);
        ModState.isGuideapiLoaded = modMap.containsKey(ModState.GUIDEAPI_ID);
        ModState.isHarvestCraftLoaded = modMap.containsKey(ModState.HARVESTCRAFT_ID);
        
        proxy.preInit(event);
        
        if (ModState.isGuideapiLoaded) {
            HCAModpackGuide.registerBook();
        }
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        LOGGER.info("It's time to get magical.");
        
        proxy.registerListeners(proxy.listeners.values());
        proxy.registerCapabilities();
        proxy.registerNetworking();
        
        proxy.init(event);
        
        if (ModState.isGuideapiLoaded) {
            HCAModpackGuide.registerRecipe();
        }
    }
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }
    
    @EventHandler
    public void serverAboutToStart(FMLServerAboutToStartEvent event) {
        proxy.serverAboutToStart(event);
    }
    
    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandTest());
        
        proxy.serverStarting(event);
        
        HardcoreAlchemyTests.setServerForEvent(event);
    }
    
    @EventHandler
    public void serverStarted(FMLServerStartedEvent event) {
        proxy.serverStarted(event);
    }
    
    @EventHandler
    public void serverStopping(FMLServerStoppingEvent event) {
        proxy.serverStopping(event);
        
        HardcoreAlchemyTests.setServerForEvent(event);
    }
}
