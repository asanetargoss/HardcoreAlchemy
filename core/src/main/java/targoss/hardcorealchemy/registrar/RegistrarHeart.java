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

package targoss.hardcorealchemy.registrar;

import java.util.List;

import org.apache.logging.log4j.Logger;

import targoss.hardcorealchemy.heart.Heart;

public class RegistrarHeart extends RegistrarForge<Heart> {
    public RegistrarHeart(String name, String namespace, Logger logger) {
        super(name, namespace, logger);
    }
    
    @Override
    public <V extends Heart> V add(String entryName, V entry) {
        entry = super.add(entryName, entry);
        entry.name = entryName;
        return entry;
    }
    
    public static class RegistryBase {
        public boolean register(List<Heart> entries) {
            return false;
        }
    }
    
    public RegistryBase IMPL = new RegistryBase();
    
    @Override
    public boolean register() {
        if (!super.register()) {
            return false;
        }
        return IMPL.register(entries);
    }
}
