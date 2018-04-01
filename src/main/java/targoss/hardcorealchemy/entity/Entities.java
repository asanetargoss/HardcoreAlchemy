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

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.Color;

import net.minecraft.entity.Entity;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.render.RenderNothing;

public class Entities {
    private static List<EntityInfo> ENTITY_CACHE = new ArrayList<>();
    
    public static final String FISH_SWARM = entity(0, EntityFishSwarm.class, "fish_swarm", new Color(0,0,0), new Color(0,0,0));
    
    private static class EntityInfo {
        int id;
        Class<? extends Entity> clazz;
        String name;
        Color primaryColor;
        Color secondaryColor;
        
        EntityInfo(int id, Class<? extends Entity> clazz, String name, Color primaryColor, Color secondaryColor) {
            this.id = id;
            this.clazz = clazz;
            this.name = name;
            this.primaryColor = primaryColor;
            this.secondaryColor = secondaryColor;
        }
    }
    
    private static String entity(int id, Class<? extends Entity> clazz, String name, Color primaryColor, Color secondaryColor) {
        EntityInfo info = new EntityInfo(id, clazz, name, primaryColor, secondaryColor);
        ENTITY_CACHE.add(info);
        
        return HardcoreAlchemy.MOD_ID + "." + name;
    }
    
    public static void registerEntities() {
        for (EntityInfo info : ENTITY_CACHE) {
            EntityRegistry.registerModEntity(info.clazz, info.name, info.id, HardcoreAlchemy.INSTANCE, 64, 3, true);
            EntityRegistry.registerEgg(info.clazz, colorValue(info.primaryColor), colorValue(info.secondaryColor));
        }
        ENTITY_CACHE.clear();
    }
    
    @SideOnly(Side.CLIENT)
    public static void registerEntityRenderers() {
        RenderingRegistry.registerEntityRenderingHandler(EntityFishSwarm.class, new RenderNothing.Factory());
    }
    
    private static int colorValue(Color color) {
        return color.getRed()*256*256 +
               color.getGreen()*256   +
               color.getBlue();
    }
}
