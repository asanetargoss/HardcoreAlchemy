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

import org.lwjgl.util.Color;

import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.coremod.PreInitLogger;

public class RegistrarEntity extends Registrar<EntityInfo> {

    public RegistrarEntity(String name, String namespace, PreInitLogger logger) {
        super(name, namespace, logger);
    }
    
    private static int colorValue(Color color) {
        return color.getRed()*256*256 +
               color.getGreen()*256   +
               color.getBlue();
    }

    @Override
    public <V extends EntityInfo> V add(String infoName, V info) {
        V result = super.add(infoName, info);
        result.name = infoName;
        result.entityName = namespace + "." + infoName;
        return result;
    }
    
    @SuppressWarnings("unchecked")
    public boolean register() {
        if (!super.register()) {
            return false;
        }

        Side side = FMLCommonHandler.instance().getSide();
        for (EntityInfo info : entries) {
            EntityRegistry.registerModEntity(info.clazz, info.name, info.id, HardcoreAlchemy.INSTANCE, 64, 3, true);
            EntityRegistry.registerEgg(info.clazz, colorValue(info.primaryColor), colorValue(info.secondaryColor));
            if (side == Side.CLIENT) {
                if (info.renderFactory != null) {
                    RenderingRegistry.registerEntityRenderingHandler(info.clazz, info.renderFactory);
                }
            }
        }
        
        return true;
    }
}
