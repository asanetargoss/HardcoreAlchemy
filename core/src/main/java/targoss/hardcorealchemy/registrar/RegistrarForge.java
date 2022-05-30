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

import org.apache.logging.log4j.Logger;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.IForgeRegistryEntry;

public class RegistrarForge<T extends IForgeRegistryEntry.Impl<T>> extends Registrar<T> {
    public RegistrarForge(String name, String namespace, Logger logger) {
        super(name, namespace, logger);
    }
    
    @Override
    public <V extends T> V add(String entryName, V entry) {
        V result = super.add(entryName, entry);
        result.setRegistryName(new ResourceLocation(namespace, entryName));
        return result;
    }
    
    public boolean register() {
        if (!super.register()) {
            return false;
        }
        
        for (T entry : entries) {
            GameRegistry.register(entry);
        }
        
        return true;
    }
}
