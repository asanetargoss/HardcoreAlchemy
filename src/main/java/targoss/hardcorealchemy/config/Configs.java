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

package targoss.hardcorealchemy.config;

import java.io.File;
import java.nio.file.Path;

import net.minecraftforge.common.config.Configuration;
import targoss.hardcorealchemy.HardcoreAlchemy;

public class Configs {
    public static final int VERSION = 2;
    public static final String BASE_CONFIG_NAME = HardcoreAlchemy.MOD_ID + ".cfg";
    
    public File hcaConfigurationDir;
    
    public ConfigBase base;
    
    public Configs() {
        base = new ConfigBase(VERSION);
    }
    
    public static int getVersionForForgeConfig(Configuration configuration, int version) {
        return configuration.getInt("version", "meta", version, 1, VERSION, "This is used to keep track of changes to the config format. Changing this is not recommended.");
    }
    
    /**
     * Initialize the configs using the given file as the base folder.
     * The base folder is assumed to be an initially empty folder exclusively used by this mod.
     */
    public void init(File hcaConfigurationDir) {
        this.hcaConfigurationDir = hcaConfigurationDir;
        Path configurationPath = hcaConfigurationDir.toPath();
        base.init(configurationPath.resolve(BASE_CONFIG_NAME).toFile());
    }
    
    public void load() {
        base.load();
    }
    
    public void save() {
        base.save();
    }
}
