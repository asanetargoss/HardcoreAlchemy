/*
 * Copyright 2017-2023 asanetargoss
 *
 * This file is part of Hardcore Alchemy Capstone.
 *
 * Hardcore Alchemy Capstone is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * Hardcore Alchemy Capstone is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Hardcore Alchemy Capstone.  If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.capstone;

import java.util.Map;

import net.minecraftforge.fml.common.ModContainer;

/**
 * Note: Not all ids have a corresponding boolean
 */
public final class CapstoneModState {
    public static final String ASTRAL_SORCERY_ID = "astralsorcery";
    public static final String GUIDEAPI_ID = "guideapi";
    public static final String IRON_BACKPACKS_ID = "ironbackpacks";
    public static boolean isAstralSorceryLoaded = false;
    public static boolean isGuideapiLoaded = false;
    public static boolean isIronBackpacksLoaded = false;
    
    protected static boolean modMapRegistered = false;

    private CapstoneModState() { }

    public static void registerModMap(Map<String, ModContainer> modMap) {
        if (modMapRegistered) {
            return;
        }
        modMapRegistered = true;

        isAstralSorceryLoaded = modMap.containsKey(ASTRAL_SORCERY_ID);
        isGuideapiLoaded = modMap.containsKey(GUIDEAPI_ID);
        isIronBackpacksLoaded = modMap.containsKey(IRON_BACKPACKS_ID);
    }
}
