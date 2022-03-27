/*
 * Copyright 2019 asanetargoss
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

package targoss.hardcorealchemy.config;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class ConfigBase {
    public Configuration configuration;
    public int version;
    
    public static final String CATEGORY_MODULES = "modules";
    public static final String MODULES_ENABLE_HEARTS = "hearts";
    public static final String MODULES_ENABLE_INSTINCTS = "instincts";

    public static final String CATEGORY_INSTINCTS = "instincts";
    public static final String INSTINCTS_FAST_DECAY = "fast_decay";

    public boolean enableHearts;
    
    public boolean enableInstincts;
    public boolean fastInstinctDecay;
    
    public ConfigBase(int version) {
        this.version = version;
    }
    
    public void init(File configurationFile) {
        configuration = new Configuration(configurationFile);
    }
    
    public void load() {
        Configs.getVersionForForgeConfig(configuration, version);
        enableHearts = configuration.getBoolean(MODULES_ENABLE_HEARTS, CATEGORY_MODULES, true, "When enabled, players have reduced starting max health depending on difficulty level, and can increase their max health with upgrades. Upgrades have a chance of being lost when a player dies. hardcorealchemy_tweaks must also be installed.");
        enableInstincts = configuration.getBoolean(MODULES_ENABLE_INSTINCTS, CATEGORY_MODULES, true, "When enabled, players stuck in permanent morphs will be subject to instincts. Instincts tell the player to do things the mob would normally do, or risk negative debuffs. hardcorealchemy_creatures must also be installed.");
        
        fastInstinctDecay = configuration.getBoolean(INSTINCTS_FAST_DECAY, CATEGORY_INSTINCTS, false, "Makes instincts always use the fast decay time when needs are not fulfilled. Useful when testing instincts.");
    }
    
    public void save() {
        configuration.save();
    }
}
