/*
 * Copyright 2017-2026 asanetargoss
 *
 * This file is part of Hardcore Alchemy LibA3.
 *
 * Hardcore Alchemy LibA3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * Hardcore Alchemy LibA3 is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License along with
 * Hardcore Alchemy LibA3.  If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.liba3;

import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import targoss.hardcorealchemy.HardcoreAlchemyCore;
import targoss.hardcorealchemy.liba3.listener.ListenerMobLists;

public class CommonProxy {
    public void preInit(FMLPreInitializationEvent event) {
        HardcoreAlchemyCore.proxy.addListener(new ListenerMobLists());
    }
}
