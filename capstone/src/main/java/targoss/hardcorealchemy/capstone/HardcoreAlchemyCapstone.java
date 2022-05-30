/*
 * Copyright 2021 asanetargoss
 * 
 * This file is part of Hardcore Alchemy modpack capstone.
 * 
 * Hardcore Alchemy modpack capstone is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation version 3 of the License.
 * 
 * Hardcore Alchemy modpack capstone is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Hardcore Alchemy modpack capstone.  If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.capstone;

import java.util.Map;

import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.ModState;
import targoss.hardcorealchemy.capstone.command.CommandTest;
import targoss.hardcorealchemy.creatures.HardcoreAlchemyCreatures;
import targoss.hardcorealchemy.magic.HardcoreAlchemyMagic;
import targoss.hardcorealchemy.survival.HardcoreAlchemySurvival;
import targoss.hardcorealchemy.tweaks.HardcoreAlchemyTweaks;

@Mod(modid = HardcoreAlchemyCapstone.MOD_ID, version = HardcoreAlchemyCapstone.VERSION,
    dependencies = HardcoreAlchemyCapstone.DEPENDENCIES, acceptedMinecraftVersions = HardcoreAlchemyCapstone.MC_VERSIONS)
public class HardcoreAlchemyCapstone
{
    public static final String MOD_ID = "hardcorealchemy_capstone";
    public static final String VERSION = "@HARDCORE_ALCHEMY_CAPSTONE_VERSION@";
    // These dependencies are replaced in capstone/build.gradle
    public static final String DEPENDENCIES = "required-before:" + HardcoreAlchemy.MOD_ID + ";" +
            "required-after:" + HardcoreAlchemyTweaks.MOD_ID + ";" +
            "required-after:" + HardcoreAlchemyCreatures.MOD_ID + ";" +
            "required-after:" + HardcoreAlchemyMagic.MOD_ID + ";" +
            "required-after:" + HardcoreAlchemySurvival.MOD_ID + ";" +
            "after:" + CapstoneModState.GUIDEAPI_ID + ";" +
            "after:" + CapstoneModState.ASTRAL_SORCERY_ID + ";" +
            "after:" + CapstoneModState.IRON_BACKPACKS_ID + ";";
    public static final String MC_VERSIONS = "[1.10.2]";
    public static final String CLIENT_PROXY = "targoss.hardcorealchemy.capstone.ClientProxy";
    public static final String COMMON_PROXY = "targoss.hardcorealchemy.capstone.CommonProxy";
    
    @Mod.Instance(HardcoreAlchemyCapstone.MOD_ID)
    public static HardcoreAlchemyCapstone INSTANCE;
    
    @SidedProxy(modId=MOD_ID, clientSide=CLIENT_PROXY, serverSide=COMMON_PROXY)
    public static CommonProxy proxy;

    public static Logger LOGGER = null;
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER = event.getModLog();
        LOGGER.info("All chakras GO!");
        
        Map<String, ModContainer> modMap = Loader.instance().getIndexedModList();
        // Normally, core is responsible for keeping track of what mods are installed,
        // but this mod needs ModState values to decide if guidebooks should be registered.
        ModState.registerModMap(modMap);
        // In addition, also keep track of the capstone-specific dependencies.
        CapstoneModState.registerModMap(modMap);
        
        proxy.preInit(event);
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }
    
    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandTest());
    }
}
