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

package targoss.hardcorealchemy.modpack.test.api;

import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import targoss.hardcorealchemy.HardcoreAlchemy;

public class UniqueFakePlayer extends FakePlayer {
    private static int ID_COUNT = 0;

    public UniqueFakePlayer(WorldServer world, GameProfile name) {
        super(world, name);
    }
    
    public static FakePlayer create() {
        MinecraftServer server = HardcoreAlchemy.SERVER_REFERENCE.get();
        WorldServer worldServer = server.worldServerForDimension(DimensionType.OVERWORLD.getId());
        
        return new UniqueFakePlayer(worldServer, new GameProfile(UUID.randomUUID(), "UniqueFakePlayer_" + String.valueOf(ID_COUNT++)));
    }
    
    @Override
    public void sendMessage(ITextComponent component) { }
}
