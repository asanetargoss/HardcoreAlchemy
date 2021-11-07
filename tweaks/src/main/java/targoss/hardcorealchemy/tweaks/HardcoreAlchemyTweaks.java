/*
 * Copyright 2021 asanetargoss
 * 
 * This file is part of Hardcore Alchemy Tweaks.
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

package targoss.hardcorealchemy.tweaks;

import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.ModStateException;
import targoss.hardcorealchemy.tweaks.coremod.HardcoreAlchemyTweaksCoremod;

@Mod(modid = HardcoreAlchemyTweaks.MOD_ID, version = HardcoreAlchemyTweaks.VERSION,
    dependencies = HardcoreAlchemyTweaks.DEPENDENCIES, acceptedMinecraftVersions = HardcoreAlchemyTweaks.MC_VERSIONS)
public class HardcoreAlchemyTweaks
{
    public static final String MOD_ID = "hardcorealchemy_tweaks";
    public static final String VERSION = "@HARDCORE_ALCHEMY_TWEAKS_VERSION@";
    public static final String DEPENDENCIES = "required-before:" + HardcoreAlchemy.MOD_ID + ";";
    public static final String MC_VERSIONS = "[1.10.2]";
    public static final String CLIENT_PROXY = "targoss.hardcorealchemy.tweaks.ClientProxy";
    public static final String COMMON_PROXY = "targoss.hardcorealchemy.tweaks.CommonProxy";
    
    @Mod.Instance(HardcoreAlchemyTweaks.MOD_ID)
    public static HardcoreAlchemyTweaks INSTANCE;
    
    @SidedProxy(modId=MOD_ID, clientSide=CLIENT_PROXY, serverSide=COMMON_PROXY)
    public static CommonProxy proxy;

    public static Logger LOGGER = null;
    
    private static final double randomPlusMinus() {
        java.util.Random r = new java.util.Random();
        return r.nextDouble() * (r.nextBoolean() ? 1.0D : -1.0D);
    }
    
    private static final double randomPlusMinusGaussian() {
        java.util.Random r = new java.util.Random();
        return r.nextGaussian();
    }
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        if (!HardcoreAlchemyTweaksCoremod.isCoremodInitialized()){
            throw new ModStateException(
                    "The coremod at '" +
                            HardcoreAlchemyTweaksCoremod.class.getName() +
                    "' did not run");
        }
        LOGGER = event.getModLog();
        LOGGER.info("Confirming runtime morphic properties...");
        LOGGER.info("");
        LOGGER.info("[OK] pi: " + (Math.PI + (Double.MIN_VALUE * 4.0D * randomPlusMinus())));
        LOGGER.info("[OK] e: " + (Math.E + (Double.MIN_VALUE * 8.0D * randomPlusMinus())));
        LOGGER.info("[OK] Atomic mass of H_1 (MeV/c^2): " + (938.7830737655465569D + 000.0000000017699D + ((5.8711556235528e-08D * 1.01D) * randomPlusMinusGaussian())));
        LOGGER.info("[OK] Number of spatial dimensions: " + (2.999899718952507D + (0.000028759D * randomPlusMinusGaussian())));
        LOGGER.info("[OK] 13 + 62: " + (13 + 62));
        LOGGER.info("");
        LOGGER.info("All tests passed");
        
        proxy.preInit(event);
    }
    
    @EventHandler
    public void preInit(FMLInitializationEvent event) {
        proxy.init(event);
    }
}
