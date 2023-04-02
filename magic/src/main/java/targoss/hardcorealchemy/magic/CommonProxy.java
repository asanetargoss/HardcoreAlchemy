/*
 * Copyright 2017-2023 asanetargoss
 *
 * This file is part of Hardcore Alchemy Magic.
 *
 * Hardcore Alchemy Magic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Magic is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Magic. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.magic;

import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import targoss.hardcorealchemy.HardcoreAlchemyCore;
import targoss.hardcorealchemy.magic.listener.ListenerInventoryExtension;
import targoss.hardcorealchemy.magic.listener.ListenerPlayerMagic;
import targoss.hardcorealchemy.magic.listener.ListenerPlayerMagicState;
import targoss.hardcorealchemy.magic.research.Studies;

public class CommonProxy {
    
    public void preInit(FMLPreInitializationEvent event) {
        HardcoreAlchemyCore.proxy.addListener(new ListenerPlayerMagic());
        HardcoreAlchemyCore.proxy.addListener(new ListenerPlayerMagicState());
        HardcoreAlchemyCore.proxy.addListener(new ListenerInventoryExtension());
        
        // Initialize via classload
        new Studies();
    }
}
