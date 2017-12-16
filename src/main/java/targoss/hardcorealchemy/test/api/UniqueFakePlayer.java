package targoss.hardcorealchemy.test.api;

import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import targoss.hardcorealchemy.test.HardcoreAlchemyTests;

public class UniqueFakePlayer extends FakePlayer {
    private static int ID_COUNT = 0;

    public UniqueFakePlayer(WorldServer world, GameProfile name) {
        super(world, name);
    }
    
    public static FakePlayer create() {
        MinecraftServer server = HardcoreAlchemyTests.SERVER_REFERENCE.get();
        WorldServer worldServer = server.worldServerForDimension(DimensionType.OVERWORLD.getId());
        
        return new UniqueFakePlayer(worldServer, new GameProfile(UUID.randomUUID(), "UniqueFakePlayer_" + String.valueOf(ID_COUNT++)));
    }
    
    @Override
    public void addChatMessage(ITextComponent component) { }
}
