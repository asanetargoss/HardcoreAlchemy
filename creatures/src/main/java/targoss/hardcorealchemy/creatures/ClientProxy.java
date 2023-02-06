/*
 * Copyright 2017-2022 asanetargoss
 *
 * This file is part of Hardcore Alchemy Creatures.
 *
 * Hardcore Alchemy Creatures is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Creatures is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Creatures. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.creatures;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import targoss.hardcorealchemy.HardcoreAlchemyCore;
import targoss.hardcorealchemy.creatures.block.Blocks;
import targoss.hardcorealchemy.creatures.item.Items;
import targoss.hardcorealchemy.creatures.listener.ListenerGuiHud;
import targoss.hardcorealchemy.creatures.listener.ListenerRenderView;

public class ClientProxy extends CommonProxy {
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        HardcoreAlchemyCore.proxy.addListener(new ListenerGuiHud());
        HardcoreAlchemyCore.proxy.addListener(new ListenerRenderView());
    }
    
    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        
        // Initialize via classload
        new Blocks.Client();
        
        Items.ClientSide.registerModels();
    }
}
