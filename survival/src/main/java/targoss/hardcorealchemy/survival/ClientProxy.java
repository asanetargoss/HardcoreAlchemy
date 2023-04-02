/*
 * Copyright 2017-2023 asanetargoss
 *
 * This file is part of Hardcore Alchemy Survival.
 *
 * Hardcore Alchemy Survival is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Survival is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Survival. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.survival;

import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import targoss.hardcorealchemy.HardcoreAlchemyCore;
import targoss.hardcorealchemy.survival.listener.ListenerGuiHud;
import targoss.hardcorealchemy.survival.listener.ListenerGuiInventory;

public class ClientProxy extends CommonProxy {
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        HardcoreAlchemyCore.proxy.addListener(new ListenerGuiHud());
        HardcoreAlchemyCore.proxy.addListener(new ListenerGuiInventory());
    }
}
