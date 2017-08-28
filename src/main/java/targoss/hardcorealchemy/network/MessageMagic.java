package targoss.hardcorealchemy.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import targoss.hardcorealchemy.listener.ListenerPlayerMagic;

public class MessageMagic extends MessageToClient {
    
    public MessageMagic() {}
    
    public boolean canUseHighMagic;
    
    public MessageMagic(boolean canUseHighMagic) {
        this.canUseHighMagic = canUseHighMagic;
    }
    
    @Override
    public void fromBytes(ByteBuf buf) {
        this.canUseHighMagic = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(canUseHighMagic);
    }
    
    public static class ReceiveAction implements Runnable {
        private boolean canUseHighMagic;
        
        public ReceiveAction(boolean canUseHighMagic) {
            this.canUseHighMagic = canUseHighMagic;
        }
        
        @Override
        public void run() {
            ListenerPlayerMagic.canUseHighMagic = this.canUseHighMagic;
        }
    }
    
    public static class Handler implements IMessageHandler<MessageMagic, IMessage> {

        @Override
        public IMessage onMessage(MessageMagic message, MessageContext ctx) {
            message.getThreadListener().addScheduledTask(new ReceiveAction(message.canUseHighMagic));
            return null;
        }
    }

    @Override
    public Class<? extends IMessageHandler<? extends MessageToClient, IMessage>> getHandlerClass() {
        return Handler.class;
    }

}
