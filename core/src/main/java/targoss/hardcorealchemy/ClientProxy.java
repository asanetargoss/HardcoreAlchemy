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

package targoss.hardcorealchemy;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import targoss.hardcorealchemy.block.Blocks;
import targoss.hardcorealchemy.entity.Entities;
import targoss.hardcorealchemy.registrar.RegistrarBlockModel;

public class ClientProxy extends CommonProxy {
    public static final ResourceLocation TILESET = new ResourceLocation(HardcoreAlchemyCore.MOD_ID, "textures/gui/icon_tileset.png");

    public ClientProxy() {
        super();
    }
    
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        Entities.ClientSide.ENTITIES.register();
    }
    
    public void init(FMLInitializationEvent event) {
        super.init(event);
        Blocks.Client.CLIENT_BLOCK_MODELS.register(RegistrarBlockModel.Client.TESR);
    }
}
