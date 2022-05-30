/*
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

import java.lang.ref.WeakReference;
import java.util.Map;

import org.apache.logging.log4j.Logger;

import net.minecraft.server.MinecraftServer;
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
import targoss.hardcorealchemy.coremod.HardcoreAlchemyCoremod;

@Mod(modid = HardcoreAlchemy.MOD_ID, version = HardcoreAlchemy.VERSION,
    dependencies = HardcoreAlchemy.DEPENDENCIES, acceptedMinecraftVersions = HardcoreAlchemy.MC_VERSIONS)
public class HardcoreAlchemy
{
    public static final String MOD_ID = "hardcorealchemy";
    public static final String SHORT_MOD_ID = "hca";
    public static final String VERSION = "@HARDCORE_ALCHEMY_VERSION@";
    public static final String DEPENDENCIES =
            /* Workaround for Potion Core canceling the armor render and
             * re-rendering it way too early (this ensures the humanity
             * bar is rendered first)
             */
            "before:potioncore;" +
            "after:" + ModState.JEI_ID + ";";
    public static final String MC_VERSIONS = "[1.10.2]";
    public static final String CLIENT_PROXY = "targoss.hardcorealchemy.ClientProxy";
    public static final String COMMON_PROXY = "targoss.hardcorealchemy.CommonProxy";
    
    @Mod.Instance(HardcoreAlchemy.MOD_ID)
    public static HardcoreAlchemy INSTANCE;
    
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
        LOGGER.info("It's time to get magical.");
        
        Map<String, ModContainer> modMap = Loader.instance().getIndexedModList();
        ModState.registerModMap(modMap);
        
        proxy.preInit(event);
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        
        proxy.init(event);
    }
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }
    
    public static WeakReference<MinecraftServer> SERVER_REFERENCE = null;
    
    @EventHandler
    public void serverAboutToStart(FMLServerAboutToStartEvent event) {
        proxy.serverAboutToStart(event);
    }
    
    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        proxy.serverStarting(event);
        
        SERVER_REFERENCE = new WeakReference<>(event.getServer());
    }
    
    @EventHandler
    public void serverStarted(FMLServerStartedEvent event) {
        proxy.serverStarted(event);
    }
    
    @EventHandler
    public void serverStopping(FMLServerStoppingEvent event) {
        proxy.serverStopping(event);
        
        SERVER_REFERENCE = null;
    }
}
