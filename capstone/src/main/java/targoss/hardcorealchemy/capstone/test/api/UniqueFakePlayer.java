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

package targoss.hardcorealchemy.capstone.test.api;

import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import targoss.hardcorealchemy.HardcoreAlchemyCore;

public class UniqueFakePlayer extends FakePlayer {
    private static int ID_COUNT = 0;

    public UniqueFakePlayer(WorldServer world, GameProfile name) {
        super(world, name);
    }
    
    public static FakePlayer create() {
        MinecraftServer server = HardcoreAlchemyCore.SERVER_REFERENCE.get();
        WorldServer worldServer = server.worldServerForDimension(DimensionType.OVERWORLD.getId());
        
        return new UniqueFakePlayer(worldServer, new GameProfile(UUID.randomUUID(), "UniqueFakePlayer_" + String.valueOf(ID_COUNT++)));
    }
    
    @Override
    public void sendMessage(ITextComponent component) { }
}
