/*
 * Copyright 2017-2023 asanetargoss
 *
 * This file is part of Hardcore Alchemy Creatures.
 *
 * Hardcore Alchemy Creatures is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Creatures is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Creatures. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.creatures.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.dmonsters.main.ModConfig;
import com.superdextor.adinferos.AdInferosReference;
import com.superdextor.adinferos.init.NetherItems;
import com.superdextor.thinkbigcore.items.ItemCustomSpawnEgg;
import com.superdextor.thinkbigcore.items.ItemCustomSpawnEgg.CustomEggEntry;

import crazypants.enderzoo.entity.MobInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Property;
import targoss.hardcorealchemy.ModState;
import targoss.hardcorealchemy.ModStateException;
import targoss.hardcorealchemy.creatures.HardcoreAlchemyCreatures;

public class EntityEggUtil {
    
    protected static class EnderZoo {
        public static void addEggs(Map<String, EntityList.EntityEggInfo> eggInfos) {
            for (MobInfo mob : MobInfo.values()) {
                if (!mob.isEnabled()) {
                    continue;
                }
                String mobName = ModState.ENDER_ZOO_ID + "." + mob.getName();
                EntityList.EntityEggInfo eggInfo = new EntityList.EntityEggInfo(mobName, mob.getEggBackgroundColor(), mob.getEggForegroundColor());
                eggInfos.put(mobName, eggInfo);
            }
        }
    }
    
    protected static class AdInferos {
        @SuppressWarnings("unchecked")
        public static void addEggs(Map<String, EntityList.EntityEggInfo> eggInfos) {
            try {
                Field spawneggEntriesField = NetherItems.class.getDeclaredField("spawneggEntries");
                spawneggEntriesField.setAccessible(true);
                ArrayList<ItemCustomSpawnEgg.CustomEggEntry> spawneggEntries = (ArrayList<CustomEggEntry>) spawneggEntriesField.get(null);
                for (ItemCustomSpawnEgg.CustomEggEntry entry : spawneggEntries) {
                    Field entityClassField = ItemCustomSpawnEgg.CustomEggEntry.class.getDeclaredField("entityClass"); 
                    entityClassField.setAccessible(true);
                    Field solidColorField = ItemCustomSpawnEgg.CustomEggEntry.class.getDeclaredField("solidColor"); 
                    solidColorField.setAccessible(true);
                    Field spotColorField = ItemCustomSpawnEgg.CustomEggEntry.class.getDeclaredField("spotColor"); 
                    spotColorField.setAccessible(true);
                    Class<? extends Entity> entityClass = (Class<? extends Entity>)entityClassField.get(entry);
                    String entityID = EntityList.CLASS_TO_NAME.get(entityClass);
                    String[] entityParts = entityID.split("\\.");
                    if (entityParts.length < 2) {
                        HardcoreAlchemyCreatures.LOGGER.warn("Unable to get Ad Inferos entity name from " + entityID);
                        continue;
                    }
                    String entityName = entityParts[1];
                    if (entityName.equals("ObsidianSheepman")) {
                        // Name in the config is different for some reason
                        entityName = "Sheepman";
                    }
                    ConfigCategory entityCategory = AdInferosReference.config.getCategory("general.Entities." + entityName);
                    Property entitySpawnWeightProperty = entityCategory.get("1. Spawn Weight");
                    if (entitySpawnWeightProperty == null) {
                        HardcoreAlchemyCreatures.LOGGER.warn("Unable to get spawn weight for Ad Inferos entity " + entityID);
                        continue;
                    }
                    int entitySpawnWeight = entitySpawnWeightProperty.getInt();
                    if (entitySpawnWeight <= 0) {
                        // This entity is most likely disabled
                        continue;
                    }
                    int solidColor = (int)solidColorField.get(entry);
                    int spotColor = (int)spotColorField.get(entry);
                    EntityList.EntityEggInfo eggInfo = new EntityList.EntityEggInfo(entityID, solidColor, spotColor);
                    eggInfos.put(entityID, eggInfo);
                }
            } catch (Exception e) {
                HardcoreAlchemyCreatures.LOGGER.warn("Unable to generate Ad Inferos spawn eggs", e);
            }
        }
    }
    
    protected static class DeadlyMonsters {
        protected static void maybeAddEgg(Map<String, EntityList.EntityEggInfo> eggInfos, String entityName, int primaryColor, int secondaryColor, boolean enabled) {
            if (!enabled) {
                return;
            }
            EntityList.EntityEggInfo eggInfo = new EntityList.EntityEggInfo(entityName, primaryColor, secondaryColor);
            eggInfos.put(eggInfo.spawnedID, eggInfo);
        }
        public static void addEggs(Map<String, EntityList.EntityEggInfo> eggInfos) {
            maybeAddEgg(eggInfos, ModState.DEADLY_MONSTERS_ID + ".mutantSteve", 0xb66d37, 0xb6a437, !ModConfig.mutantSteveDisabled);
            maybeAddEgg(eggInfos, ModState.DEADLY_MONSTERS_ID + ".freezer", 0x1b729e, 0x662c36,!ModConfig.freezerDisabled);
            maybeAddEgg(eggInfos, ModState.DEADLY_MONSTERS_ID + ".climber", 0x38345d, 0xd40909, !ModConfig.climberDisabled);
            maybeAddEgg(eggInfos, ModState.DEADLY_MONSTERS_ID + ".zombieChicken", 0x319531, 0x266427, !ModConfig.zombieChickenDisabled);
            maybeAddEgg(eggInfos, ModState.DEADLY_MONSTERS_ID + ".baby", 0xd23737, 0xba2929, !ModConfig.babyDisabled);
            maybeAddEgg(eggInfos, ModState.DEADLY_MONSTERS_ID + ".wideman", 0xaa9e79, 0x837964, !ModConfig.fallenLeaderDisabled);
            maybeAddEgg(eggInfos, ModState.DEADLY_MONSTERS_ID + ".woman", 0xdbb795, 0x806d5c, !ModConfig.bloodyMaidenDisabled);
            maybeAddEgg(eggInfos, ModState.DEADLY_MONSTERS_ID + ".entrail", 0xc65539, 0xc66f38, !ModConfig.entrailDisabled);
            maybeAddEgg(eggInfos, ModState.DEADLY_MONSTERS_ID + ".present", 0x3c7c42, 0xc5ee15, !ModConfig.presentDisabled);
            maybeAddEgg(eggInfos, ModState.DEADLY_MONSTERS_ID + ".stranger", 0xbe3d0e, 0x543f02, !ModConfig.strangerDisabled);
            maybeAddEgg(eggInfos, ModState.DEADLY_MONSTERS_ID + ".hauntedcow", 0x150c1c, 0x9c6cbe, !ModConfig.hauntedCowDisabled);
            maybeAddEgg(eggInfos, ModState.DEADLY_MONSTERS_ID + ".topielec", 0x327232, 0x5b572f, !ModConfig.topielecDisabled);
        }
    }
    
    protected static Map<String, EntityList.EntityEggInfo> EGG_INFOS;
    
    protected static void initEggInfo() {
        if (EntityList.ENTITY_EGGS.isEmpty()) {
            throw new ModStateException("Entity eggs are not finished registering");
        }
        // LinkedHashMap maintains the order when iterating, for a more organized JEI view.
        EGG_INFOS = new LinkedHashMap<>();
        EGG_INFOS.putAll(EntityList.ENTITY_EGGS);
        if (ModState.isEnderZooLoaded) {
            EnderZoo.addEggs(EGG_INFOS);
        }
        if (ModState.isAdInferosLoaded) {
            AdInferos.addEggs(EGG_INFOS);
        }
        if (ModState.isDeadlyMonstersLoaded) {
            DeadlyMonsters.addEggs(EGG_INFOS);
        }
    }
    
    public static Map<String, EntityList.EntityEggInfo> getEggInfos() {
        if (EGG_INFOS == null) {
            initEggInfo();
        }
        return EGG_INFOS;
    }
    
    public static @Nullable EntityList.EntityEggInfo getEggInfo(String entityID) {
        if (EGG_INFOS == null) {
            initEggInfo();
        }
        return EGG_INFOS.get(entityID);
    }
}
