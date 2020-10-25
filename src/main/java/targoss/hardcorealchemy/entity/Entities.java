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

import org.lwjgl.util.Color;

import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.render.RenderNothing;
import targoss.hardcorealchemy.util.EntityInfo;
import targoss.hardcorealchemy.util.Registrar;
import targoss.hardcorealchemy.util.RegistrarEntity;

public class Entities {
    public static final Registrar<EntityInfo> ENTITIES = new RegistrarEntity("entities", HardcoreAlchemy.MOD_ID, HardcoreAlchemy.PRE_INIT_LOGGER);
    
    public static final String FISH_SWARM = ENTITIES.add("fish_swarm", new EntityInfo(0, EntityFishSwarm.class, new RenderNothing.Factory(), new Color(0,0,0), new Color(0,0,0))).entityName;
}
