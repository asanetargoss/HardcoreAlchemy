/*
 * Copyright 2017-2018 asanetargoss
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

package targoss.hardcorealchemy.network;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import targoss.hardcorealchemy.HardcoreAlchemy;

public class PacketHandler {
    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(HardcoreAlchemy.MOD_ID);
    private static int idCount = 0;
    
    public static int getNextId() {
        return idCount++;
    }
    
    public static void register() {
        PacketHandler packetHandler = new PacketHandler();
        (new MessageHumanity()).register();
        (new MessageKillCount()).register();
        (new MessageMorphState()).register();
        (new MessageInactiveCapabilities()).register();
        (new MessageInstinct()).register();
        (new MessageInstinctNeedState()).register();
        (new MessageInstinctEffects()).register();
        (new MessageInstinctNeedChanged()).register();
        (new MessageConfigs()).register();
    }
    
}
