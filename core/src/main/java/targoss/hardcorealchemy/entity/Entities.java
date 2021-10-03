/*
 * Copyright 2018 asanetargoss
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

package targoss.hardcorealchemy.entity;

import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.coremod.HardcoreAlchemyPreInit;
import targoss.hardcorealchemy.registrar.Registrar;
import targoss.hardcorealchemy.registrar.RegistrarEntity;
import targoss.hardcorealchemy.render.RenderNothing;
import targoss.hardcorealchemy.util.Color;
import targoss.hardcorealchemy.util.EntityInfo;

public class Entities {
    public static final Registrar<EntityInfo> ENTITIES = new RegistrarEntity("entities", HardcoreAlchemy.MOD_ID, HardcoreAlchemyPreInit.LOGGER);
    
    public static final EntityInfo FISH_SWARM_ENTRY = ENTITIES.add("fish_swarm", new EntityInfo(0, EntityFishSwarm.class, new Color(0,0,0), new Color(0,0,0)));
    public static final String FISH_SWARM = FISH_SWARM_ENTRY.entityName;
    
    public static class ClientSide {
        public static final Registrar<EntityInfo.ClientSide> ENTITIES = new RegistrarEntity.ClientSide("entities_client", HardcoreAlchemy.MOD_ID, HardcoreAlchemyPreInit.LOGGER);
        
        public static final EntityInfo.ClientSide FISH_SWARM_ENTRY = ENTITIES.add(Entities.FISH_SWARM_ENTRY.name, new EntityInfo.ClientSide(Entities.FISH_SWARM_ENTRY, new RenderNothing.Factory()));
    }
}
