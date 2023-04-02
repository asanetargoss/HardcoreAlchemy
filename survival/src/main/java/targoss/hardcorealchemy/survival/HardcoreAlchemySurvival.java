/*
 * Copyright 2017-2023 asanetargoss
 *
 * This file is part of Hardcore Alchemy Survival.
 *
 * Hardcore Alchemy Survival is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Survival is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Survival. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.survival;

import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import targoss.hardcorealchemy.HardcoreAlchemyCore;
import targoss.hardcorealchemy.ModState;
import targoss.hardcorealchemy.ModStateException;
import targoss.hardcorealchemy.survival.coremod.HardcoreAlchemySurvivalCoremod;

@Mod(modid = HardcoreAlchemySurvival.MOD_ID, version = HardcoreAlchemySurvival.VERSION,
    dependencies = HardcoreAlchemySurvival.DEPENDENCIES, acceptedMinecraftVersions = HardcoreAlchemySurvival.MC_VERSIONS)
public class HardcoreAlchemySurvival
{
    public static final String MOD_ID = "hardcorealchemy_survival";
    public static final String VERSION = "@HARDCORE_ALCHEMY_SURVIVAL_VERSION@";
    public static final String DEPENDENCIES = "required-before:" + HardcoreAlchemyCore.MOD_ID + ";" +
            "after:" + ModState.TAN_ID + ";" +
            "after:uniquecrops;" +
            "after:" + ModState.HARVESTCRAFT_ID + ";";
    public static final String MC_VERSIONS = "[1.10.2]";
    public static final String CLIENT_PROXY = "targoss.hardcorealchemy.survival.ClientProxy";
    public static final String COMMON_PROXY = "targoss.hardcorealchemy.survival.CommonProxy";
    
    @Mod.Instance(HardcoreAlchemySurvival.MOD_ID)
    public static HardcoreAlchemySurvival INSTANCE;
    
    @SidedProxy(modId=MOD_ID, clientSide=CLIENT_PROXY, serverSide=COMMON_PROXY)
    public static CommonProxy proxy;

    public static Logger LOGGER = null;
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        if (!HardcoreAlchemySurvivalCoremod.isCoremodInitialized()){
            throw new ModStateException(
                    "The coremod at '" +
                            HardcoreAlchemySurvivalCoremod.class.getName() +
                    "' did not run");
        }
        LOGGER = event.getModLog();
        LOGGER.info("Please remember to drink water.");
        
        proxy.preInit(event);
    }
}
