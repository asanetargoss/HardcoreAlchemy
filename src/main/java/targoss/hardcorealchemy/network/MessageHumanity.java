package targoss.hardcorealchemy.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import targoss.hardcorealchemy.listener.ListenerGui;

public class MessageHumanity extends MessageToClient {
    
    public MessageHumanity() {}
    
    public boolean render_humanity;
    public double humanity;
    public double max_humanity;
    
    public MessageHumanity(boolean render_humanity, double humanity, double max_humanity) {
        this.render_humanity = render_humanity;
        this.humanity = humanity;
        this.max_humanity = max_humanity;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(render_humanity);
        buf.writeDouble(humanity);
        buf.writeDouble(max_humanity);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        render_humanity = buf.readBoolean();
        humanity = buf.readDouble();
        max_humanity = buf.readDouble();
    }
    
    public static class ReceiveAction implements Runnable {
        private boolean render_humanity;
        private double humanity;
        private double max_humanity;
        
        public ReceiveAction(boolean render_humanity, double humanity, double max_humanity) {
            this.render_humanity = render_humanity;
            this.humanity = humanity;
            this.max_humanity = max_humanity;
        }
        
        @Override
        public void run() {
            ListenerGui.render_humanity = render_humanity;
            ListenerGui.humanity = humanity;
            ListenerGui.max_humanity = max_humanity;
        }
    }
    
    public static class Handler implements IMessageHandler<MessageHumanity, IMessage> {
        @Override
        public IMessage onMessage(MessageHumanity message, MessageContext ctx) {
            message.getThreadListener().addScheduledTask(new ReceiveAction(message.render_humanity, message.humanity, message.max_humanity));
            return null;
        }
    }

    @Override
    public Class<? extends IMessageHandler<? extends MessageToClient, IMessage>> getHandlerClass() {
        return Handler.class;
    }
}
