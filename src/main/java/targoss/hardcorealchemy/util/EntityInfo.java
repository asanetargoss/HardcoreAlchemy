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

package targoss.hardcorealchemy.util;

import net.minecraft.entity.Entity;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class EntityInfo {
    public final int id;
    public final Class<? extends Entity> clazz;
    @SuppressWarnings("rawtypes")
    public final IRenderFactory renderFactory;
    public final Color primaryColor;
    public final Color secondaryColor;

    public String name;
    public String entityName;
    
    public EntityInfo(int id, Class<? extends Entity> clazz, @SuppressWarnings("rawtypes") IRenderFactory renderFactory, Color primaryColor, Color secondaryColor) {
        this.id = id;
        this.clazz = clazz;
        this.renderFactory = renderFactory;
        this.primaryColor = primaryColor;
        this.secondaryColor = secondaryColor;
    }
}