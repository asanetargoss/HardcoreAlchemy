/*
 * Copyright 2017-2022 asanetargoss
 *
 * This file is part of Hardcore Alchemy Capstone.
 *
 * Hardcore Alchemy Capstone is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * Hardcore Alchemy Capstone is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Hardcore Alchemy Capstone.  If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.capstone;

import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.capstone.listener.ListenerGuides;
import targoss.hardcorealchemy.capstone.listener.ListenerInventoryExtension;
import targoss.hardcorealchemy.capstone.listener.ListenerPlayerInventory;
import targoss.hardcorealchemy.capstone.listener.ListenerPlayerMagicState;

public class CommonProxy {
    public void preInit(FMLPreInitializationEvent event) {
        HardcoreAlchemy.proxy.addListener(new ListenerPlayerInventory());
        HardcoreAlchemy.proxy.addListener(new ListenerInventoryExtension());
        HardcoreAlchemy.proxy.addListener(new ListenerPlayerMagicState());
        HardcoreAlchemy.proxy.addListener(new ListenerGuides());
    }
}
