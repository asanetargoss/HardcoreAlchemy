/*
 * Copyright 2021 asanetargoss
 * 
 * This file is part of Hardcore Alchemy Magic.
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

package targoss.hardcorealchemy.magic;

import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.ModState;
import targoss.hardcorealchemy.ModStateException;
import targoss.hardcorealchemy.magic.coremod.HardcoreAlchemyMagicCoremod;

@Mod(modid = HardcoreAlchemyMagic.MOD_ID, version = HardcoreAlchemyMagic.VERSION,
    dependencies = HardcoreAlchemyMagic.DEPENDENCIES, acceptedMinecraftVersions = HardcoreAlchemyMagic.MC_VERSIONS)
public class HardcoreAlchemyMagic
{
    public static final String MOD_ID = "hardcorealchemy-magic";
    public static final String VERSION = "@HARDCORE_ALCHEMY_MAGIC_VERSION@";
    public static final String DEPENDENCIES = "required-before:" + HardcoreAlchemy.MOD_ID + ";" +
            "after:" + ModState.ASTRAL_SORCERY_ID + ";" +
            "after:" + ModState.BLOOD_MAGIC_ID + ";" +
            "after:" + ModState.ARS_MAGICA_ID + ";" +
            "after:" + ModState.PROJECT_E_ID + ";" +
            "after:" + ModState.ARS_MAGICA_ID + ";" +
            "after:" + ModState.THAUMCRAFT_ID + ";";
    public static final String MC_VERSIONS = "[1.10.2]";
    public static final String CLIENT_PROXY = "targoss.hardcorealchemy.magic.ClientProxy";
    public static final String COMMON_PROXY = "targoss.hardcorealchemy.magic.CommonProxy";
    
    @Mod.Instance(HardcoreAlchemyMagic.MOD_ID)
    public static HardcoreAlchemyMagic INSTANCE;
    
    @SidedProxy(modId=MOD_ID, clientSide=CLIENT_PROXY, serverSide=COMMON_PROXY)
    public static CommonProxy proxy;

    public static Logger LOGGER = null;
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        if (!HardcoreAlchemyMagicCoremod.isCoremodInitialized()){
            throw new ModStateException(
                    "The coremod at '" +
                            HardcoreAlchemyMagicCoremod.class.getName() +
                    "' did not run");
        }
        LOGGER = event.getModLog();
        LOGGER.info("The phase of the moon has been recorded for later debugging.");
        
        proxy.preInit(event);
    }
}
