package targoss.hardcorealchemy.network;

import net.minecraft.client.Minecraft;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.relauncher.Side;

public abstract class MessageToClient implements IMessage {
    public void register(PacketHandler packetHandler) {
        packetHandler.INSTANCE.registerMessage(
                (Class<IMessageHandler<MessageToClient, IMessage>>)this.getHandlerClass(), (Class<MessageToClient>)this.getClass(), packetHandler.getNextId(), Side.CLIENT
                );
    }
    
    public static IThreadListener getThreadListener() {
        return Minecraft.getMinecraft();
    }
    
    public abstract Class<? extends IMessageHandler<? extends MessageToClient, IMessage>> getHandlerClass();
}
