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

package targoss.hardcorealchemy.coremod;

import javax.annotation.Nonnull;

/**
 * Object for storing and retrieving a method/function name.
 * Useful when working with Minecraft methods and fields, whose
 * value can be a srgname in a release environment but an MCP
 * name in a development environment.
 */
public class ObfuscatedName {
    private final String srgName;
    /** Initially null. Given cached value when get()
     * is called in a development environment */
    private String mcpName = null;
    
    public ObfuscatedName(@Nonnull String srgName) {
        this.srgName = srgName;
    }
    
    /**
     * Get name relevant for current environment.
     * In development environment, default to srgName
     * if mcpName is not present in the mapping.
     */
    public @Nonnull String get() {
        if (HardcoreAlchemyCoremod.obfuscated) {
            return srgName;
        }
        else {
            if (mcpName == null) {
                mcpName = DevMappings.get(srgName);
                if (mcpName == null) {
                    mcpName = srgName;
                }
            }
            return mcpName;
        }
    }
}
