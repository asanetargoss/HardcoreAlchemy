/*
 * Copyright 2017-2023 asanetargoss
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

package targoss.hardcorealchemy.entity;

import net.minecraft.entity.Entity;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import targoss.hardcorealchemy.util.Color;

public class EntityInfo {
    public final int id;
    public final Class<? extends Entity> clazz;
    public final Color primaryColor;
    public final Color secondaryColor;

    public String name;
    public String entityName;
    
    public EntityInfo(int id, Class<? extends Entity> clazz, Color primaryColor, Color secondaryColor) {
        this.id = id;
        this.clazz = clazz;
        this.primaryColor = primaryColor;
        this.secondaryColor = secondaryColor;
    }
    
    public static class ClientSide {
        public final EntityInfo info;
        @SuppressWarnings("rawtypes")
        public final IRenderFactory renderFactory;
        
        public ClientSide(EntityInfo info, @SuppressWarnings("rawtypes") IRenderFactory renderFactory) {
            this.info = info;
            this.renderFactory = renderFactory;
        }
    }
}