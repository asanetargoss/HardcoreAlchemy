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

public class ObfuscatedName {
    private final String srgName;
    private String mcpName = null;
    
    public ObfuscatedName(String srgName) {
        this.srgName = srgName;
    }
    
    public String get() {
        return HardcoreAlchemyCoremod.obfuscated ? srgName : getMcpName();
    }
    
    private String getMcpName() {
        if (mcpName == null) {
            mcpName = DevMappings.get(srgName);
        }
        return mcpName;
    }
}
