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

import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.util.EntityInfo;
import targoss.hardcorealchemy.util.EntityInfo.ClientSide;

public class RegistrarEntity extends Registrar<EntityInfo> {

    public RegistrarEntity(String name, String namespace, Logger logger) {
        super(name, namespace, logger);
    }

    @Override
    public <V extends EntityInfo> V add(String infoName, V info) {
        V result = super.add(infoName, info);
        result.name = infoName;
        result.entityName = namespace + "." + infoName;
        return result;
    }
    
    public boolean register() {
        if (!super.register()) {
            return false;
        }

        for (EntityInfo info : entries) {
            EntityRegistry.registerModEntity(info.clazz, info.name, info.id, HardcoreAlchemy.INSTANCE, 64, 3, true);
            EntityRegistry.registerEgg(info.clazz, info.primaryColor.toPackedRGB(), info.secondaryColor.toPackedRGB());
        }
        
        return true;
    }
    
    public static class ClientSide extends Registrar<EntityInfo.ClientSide> {
        public ClientSide(String name, String namespace, Logger logger) {
            super(name, namespace, logger);
        }
        
        @SuppressWarnings("unchecked")
        public boolean register() {
            if (!super.register()) {
                return false;
            }

            for (EntityInfo.ClientSide infoClient : entries) {
                if (infoClient.renderFactory != null) {
                    RenderingRegistry.registerEntityRenderingHandler(infoClient.info.clazz, infoClient.renderFactory);
                }
            }
            
            return true;
        }
    }
}
