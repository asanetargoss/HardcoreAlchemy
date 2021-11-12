package targoss.hardcorealchemy.creatures.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import targoss.hardcorealchemy.creatures.HardcoreAlchemyCreatures;
import targoss.hardcorealchemy.creatures.listener.ListenerPlayerMorphs;
import targoss.hardcorealchemy.network.MessageToClient;
import targoss.hardcorealchemy.util.MiscVanilla;

public class MessageMaxHumanity extends MessageToClient<HardcoreAlchemyCreatures> {
    public MessageMaxHumanity() {}

    @Override
    public void fromBytes(ByteBuf buf) {}

    @Override
    public void toBytes(ByteBuf buf) {}
    
    public static class ReceiveAction implements Runnable {
        public static final ReceiveAction INSTANCE = new ReceiveAction();
        @Override
        public void run() {
            ListenerPlayerMorphs.updateMaxHumanity(MiscVanilla.getTheMinecraftPlayer());
        }
    }
    
    public static class Handler implements IMessageHandler<MessageMaxHumanity, IMessage> {
        @Override
        public IMessage onMessage(MessageMaxHumanity message, MessageContext ctx) {
            message.getThreadListener().addScheduledTask(ReceiveAction.INSTANCE);
            return null;
        }
    }

    @Override
    public Class<? extends IMessageHandler<? extends MessageToClient<HardcoreAlchemyCreatures>, IMessage>> getHandlerClass() {
        return Handler.class;
    }

}
