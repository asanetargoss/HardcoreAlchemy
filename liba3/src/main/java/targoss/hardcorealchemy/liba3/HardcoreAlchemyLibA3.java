/*
 * Copyright 2017-2025 asanetargoss
 *
 * This file is part of Hardcore Alchemy LibA3.
 *
 * Hardcore Alchemy LibA3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * Hardcore Alchemy LibA3 is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License along with
 * Hardcore Alchemy LibA3.  If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.liba3;

import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import targoss.hardcorealchemy.HardcoreAlchemyCore;

@Mod(modid = HardcoreAlchemyLibA3.MOD_ID, version = HardcoreAlchemyLibA3.VERSION,
    dependencies = HardcoreAlchemyLibA3.DEPENDENCIES, acceptedMinecraftVersions = HardcoreAlchemyLibA3.MC_VERSIONS)
public class HardcoreAlchemyLibA3
{
    public static final String MOD_ID = "hardcorealchemy_liba3";
    public static final String VERSION = "@HARDCORE_ALCHEMY_LIBA3_VERSION@";
    public static final String DEPENDENCIES = "required-before:" + HardcoreAlchemyCore.MOD_ID + ";";
    public static final String MC_VERSIONS = "[1.10.2]";
    public static final String CLIENT_PROXY = "targoss.hardcorealchemy.liba3.ClientProxy";
    public static final String COMMON_PROXY = "targoss.hardcorealchemy.liba3.CommonProxy";
    
    @Mod.Instance(HardcoreAlchemyLibA3.MOD_ID)
    public static HardcoreAlchemyLibA3 INSTANCE;
    
    @SidedProxy(modId=MOD_ID, clientSide=CLIENT_PROXY, serverSide=COMMON_PROXY)
    public static CommonProxy proxy;

    public static Logger LOGGER = null;
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER = event.getModLog();
        LOGGER.info("This isn't even my final form!");
        
        proxy.preInit(event);
    }
}
