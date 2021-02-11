/*
 * Copyright 2020 asanetargoss
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

package targoss.hardcorealchemy.registrar;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;

public abstract class Registrar<T> {
    /** Human-readable name */
    protected final String name;
    protected final String namespace;
    protected final Logger logger;
    protected boolean registered;
    protected List<T> entries = new ArrayList<T>();
    
    public Registrar(String name, String namespace, Logger logger) {
        this.name = name;
        this.namespace = namespace;
        this.logger = logger;
    }
    
    public <V extends T> V add(String entryName, V entry) {
        logger.debug("Adding registry entry '" + entryName + "' to registrar '" + name + "'");
        entries.add(entry);
        return entry;
    }
    
    public boolean register() {
        if (registered) {
            logger.warn("Registrar '" + name + "' already registered.");
            return false;
        }
        registered = true;
        
        return true;
    }
}
