package targoss.hardcorealchemy.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
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
    }
    
}
