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
import targoss.hardcorealchemy.command.CommandTest;
import targoss.hardcorealchemy.coremod.HardcoreAlchemyCoremod;
import targoss.hardcorealchemy.entity.Entities;
import targoss.hardcorealchemy.incantation.Incantations;
import targoss.hardcorealchemy.instinct.Instincts;
import targoss.hardcorealchemy.item.Items;
import targoss.hardcorealchemy.metamorph.HcAMetamorphPack;
import targoss.hardcorealchemy.modpack.guide.AlchemicAshGuide;
import targoss.hardcorealchemy.modpack.guide.HCAModpackGuide;
import targoss.hardcorealchemy.modpack.guide.HCAUpgradeGuides;
import targoss.hardcorealchemy.registrar.RegistrarUpgradeGuide;
import targoss.hardcorealchemy.research.Studies;

@Mod(modid = HardcoreAlchemy.MOD_ID, version = HardcoreAlchemy.VERSION,
    dependencies = HardcoreAlchemy.DEPENDENCIES, acceptedMinecraftVersions = HardcoreAlchemy.MC_VERSIONS)
public class HardcoreAlchemy
{
    public static final String MOD_ID = "hardcorealchemy";
    public static final String VERSION = "@HARDCORE_ALCHEMY_VERSION@";
    public static final String DEPENDENCIES = "required-after:metamorph@[1.2.3,);" +
         /* If/when TaN for 1.10 updates to version 4.x.x, 
          * remove crop decay code, NPE fix, and thirst overlay fix.
          * Then, the version specifier should be updated to require 4.x.x
          */
            "after:" + ModState.TAN_ID + "@1.1.1.19;" +
          /* Workaround for Potion Core canceling the armor render and
           * re-rendering it way too early (this ensures the humanity
           * bar is rendered first)
           */
            "before:potioncore;" +
            "after:astralsorcery;" +
            "after:adinferos;" +
            "after:uniquecrops;" +
            "after:" + ModState.GUIDEAPI_ID + ";" +
            "after:" + ModState.BLOOD_MAGIC_ID + ";" +
            "after:" + ModState.ARS_MAGICA_ID + ";" +
            "after:" + ModState.PROJECT_E_ID + ";" +
            "after:" + ModState.ARS_MAGICA_ID + ";" +
            "after:" + ModState.HARVESTCRAFT_ID + ";" +
            "after:" + ModState.THAUMCRAFT_ID + ";" +
            "after:" + ModState.JEI_ID + ";" +
            "after:" + ModState.ASTRAL_SORCERY_ID + ";";
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
        ModState.isThaumcraftLoaded = modMap.containsKey(ModState.THAUMCRAFT_ID);
        ModState.isAlchemicAshLoaded = modMap.containsKey(ModState.ALCHEMIC_ASH_ID);
        ModState.isJEILoaded = modMap.containsKey(ModState.JEI_ID);
        ModState.isSpiceOfLifeLoaded = modMap.containsKey(ModState.SPICE_OF_LIFE_ID);
        ModState.isAstralSorceryLoaded = modMap.containsKey(ModState.ASTRAL_SORCERY_ID);
        
        proxy.preInit(event);
        
        Items.ITEMS.register();
        Items.POTIONS.register();
        Items.POTION_TYPES.register();
        Entities.ENTITIES.register();
        Studies.KNOWLEDGE_FACTS.register();
        
        if (ModState.isGuideapiLoaded) {
            HCAModpackGuide.preInit();
            HCAUpgradeGuides.UPGRADE_GUIDES.register(RegistrarUpgradeGuide.BOOK_AND_MODEL);
        }
        
        if (ModState.isGuideapiLoaded && ModState.isAlchemicAshLoaded) {
            AlchemicAshGuide.preInit();
        }
        
        proxy.registerNetworking();
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        LOGGER.info("It's time to get magical.");
        
        proxy.registerListeners(proxy.listeners.values());
        proxy.registerCapabilities();
        
        proxy.init(event);
        
        Items.registerRecipes();
        HcAMetamorphPack.registerAbilities();
        // Why are these in init and not pre-init?
        Instincts.INSTINCTS.register();
        Instincts.INSTINCT_NEED_FACTORIES.register();
        Instincts.INSTINCT_EFFECTS.register();
        Incantations.INCANTATIONS.register();
        
        if (ModState.isGuideapiLoaded) {
            HCAModpackGuide.init();
            HCAUpgradeGuides.UPGRADE_GUIDES.register(RegistrarUpgradeGuide.RECIPES);
            HCAUpgradeGuides.UPGRADE_GUIDES.register(RegistrarUpgradeGuide.CATEGORIES);
            HCAUpgradeGuides.UPGRADE_GUIDES.register(RegistrarUpgradeGuide.CLEANUP);
        }
        
        if (ModState.isGuideapiLoaded && ModState.isAlchemicAshLoaded) {
            AlchemicAshGuide.init();
        }
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
        event.registerServerCommand(new CommandTest());
        
        proxy.serverStarting(event);
        
        SERVER_REFERENCE = new WeakReference(event.getServer());
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
