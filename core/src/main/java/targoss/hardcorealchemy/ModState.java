/*
 * Copyright 2017-2022 asanetargoss
 *
 * This file is part of Hardcore Alchemy Core.
 *
 * Hardcore Alchemy Core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Core is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Core. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy;

import java.util.Map;

import net.minecraftforge.fml.common.ModContainer;

/**
 * Note: Not all ids have a corresponding boolean
 */
public final class ModState {
    public static final String ADINFEROS_ID = "adinferos";
    public static final String ALCHEMIC_ASH_ID = "AlchemicAsh";
    public static final String ARS_MAGICA_ID = "arsmagica2";
    public static final String BLOOD_MAGIC_ID = "BloodMagic";
    public static final String DEADLY_MONSTERS_ID = "dmonsters";
    public static final String DISSOLUTION_ID = "dissolution";
    public static final String ENDER_ZOO_ID = "EnderZoo";
    public static final String HARVESTCRAFT_ID = "harvestcraft";
    public static final String JEI_ID = "JEI";
    public static final String MANTLE_ID = "mantle";
    public static final String METAMORPH_ID = "metamorph";
    public static final String NUTRITION_ID = "nutrition";
    public static final String PROJECT_E_ID = "ProjectE";
    public static final String SPICE_OF_LIFE_ID = "SpiceOfLife";
    public static final String TAN_ID = "ToughAsNails";
    public static final String THAUMCRAFT_ID = "thaumcraft";
    public static final String WAWLA_ID = "Waila";
    public static boolean isAlchemicAshLoaded = false;
    public static boolean isArsMagicaLoaded = false;
    public static boolean isBloodMagicLoaded = false;
    public static boolean isDissolutionLoaded = false;
    public static boolean isHarvestCraftLoaded = false;
    public static boolean isJEILoaded = false;
    public static boolean isMantleLoaded = false;
    public static boolean isMetamorphLoaded = false;
    public static boolean isNutritionLoaded = false;
    public static boolean isProjectELoaded = false;
    public static boolean isSpiceOfLifeLoaded = false;
    public static boolean isTanLoaded = false;
    public static boolean isThaumcraftLoaded = false;
    public static boolean isWawlaLoaded = false;
    
    protected static boolean modMapRegistered = false;

    private ModState() { }

    public static void registerModMap(Map<String, ModContainer> modMap) {
        if (modMapRegistered) {
            return;
        }
        modMapRegistered = true;
        
        isAlchemicAshLoaded = modMap.containsKey(ALCHEMIC_ASH_ID);
        isArsMagicaLoaded = modMap.containsKey(ARS_MAGICA_ID);
        isBloodMagicLoaded = modMap.containsKey(BLOOD_MAGIC_ID);
        isDissolutionLoaded = modMap.containsKey(DISSOLUTION_ID);
        isHarvestCraftLoaded = modMap.containsKey(HARVESTCRAFT_ID);
        isJEILoaded = modMap.containsKey(JEI_ID);
        isMantleLoaded = modMap.containsKey(MANTLE_ID);
        isMetamorphLoaded = modMap.containsKey(METAMORPH_ID);
        isNutritionLoaded = modMap.containsKey(NUTRITION_ID);
        isProjectELoaded = modMap.containsKey(PROJECT_E_ID);
        isSpiceOfLifeLoaded = modMap.containsKey(SPICE_OF_LIFE_ID);
        isTanLoaded = modMap.containsKey(TAN_ID);
        isThaumcraftLoaded = modMap.containsKey(THAUMCRAFT_ID);
        isWawlaLoaded = modMap.containsKey(WAWLA_ID);
    }
}
