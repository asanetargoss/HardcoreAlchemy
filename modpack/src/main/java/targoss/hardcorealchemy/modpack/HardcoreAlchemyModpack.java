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

package targoss.hardcorealchemy.modpack;

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
import targoss.hardcorealchemy.creatures.HardcoreAlchemyCreatures;
import targoss.hardcorealchemy.magic.HardcoreAlchemyMagic;
import targoss.hardcorealchemy.modpack.command.CommandTest;
import targoss.hardcorealchemy.survival.HardcoreAlchemySurvival;
import targoss.hardcorealchemy.tweaks.HardcoreAlchemyTweaks;

@Mod(modid = HardcoreAlchemyModpack.MOD_ID, version = HardcoreAlchemyModpack.VERSION,
    dependencies = HardcoreAlchemyModpack.DEPENDENCIES, acceptedMinecraftVersions = HardcoreAlchemyModpack.MC_VERSIONS)
public class HardcoreAlchemyModpack
{
    public static final String MOD_ID = "hardcorealchemy_modpack";
    public static final String VERSION = "@HARDCORE_ALCHEMY_MODPACK_VERSION@";
    public static final String DEPENDENCIES = "required-before:" + HardcoreAlchemy.MOD_ID + ";" +
            "required-after:" + HardcoreAlchemyTweaks.MOD_ID + ";" +
            "required-after:" + HardcoreAlchemyCreatures.MOD_ID + ";" +
            "required-after:" + HardcoreAlchemyMagic.MOD_ID + ";" +
            "required-after:" + HardcoreAlchemySurvival.MOD_ID + ";";
    public static final String MC_VERSIONS = "[1.10.2]";
    public static final String CLIENT_PROXY = "targoss.hardcorealchemy.modpack.ClientProxy";
    public static final String COMMON_PROXY = "targoss.hardcorealchemy.modpack.CommonProxy";
    
    @Mod.Instance(HardcoreAlchemyModpack.MOD_ID)
    public static HardcoreAlchemyModpack INSTANCE;
    
    @SidedProxy(modId=MOD_ID, clientSide=CLIENT_PROXY, serverSide=COMMON_PROXY)
    public static CommonProxy proxy;

    public static Logger LOGGER = null;
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER = event.getModLog();
        LOGGER.info("All chakras GO!");
        
        // Normally, core is responsible for keeping track of what mods are installed,
        // but this mod needs ModState values to decide if guidebooks should be registered.
        Map<String, ModContainer> modMap = Loader.instance().getIndexedModList();
        ModState.registerModMap(modMap);
        
        proxy.preInit(event);
    }
    
    @EventHandler
    public void preInit(FMLInitializationEvent event) {
        proxy.init(event);
    }
    
    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandTest());
    }
}
