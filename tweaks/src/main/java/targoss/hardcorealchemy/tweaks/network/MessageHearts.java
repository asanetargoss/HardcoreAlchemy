/*
 * Copyright 2017-2022 asanetargoss
 *
 * This file is part of Hardcore Alchemy Tweaks.
 *
 * Hardcore Alchemy Tweaks is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Tweaks is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Tweaks. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.tweaks.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import targoss.hardcorealchemy.HardcoreAlchemyCore;
import targoss.hardcorealchemy.network.MessageToClient;
import targoss.hardcorealchemy.tweaks.HardcoreAlchemyTweaks;
import targoss.hardcorealchemy.tweaks.capability.hearts.ICapabilityHearts;
import targoss.hardcorealchemy.tweaks.capability.hearts.ProviderHearts;
import targoss.hardcorealchemy.tweaks.capability.hearts.StorageHearts;
import targoss.hardcorealchemy.tweaks.listener.ListenerHearts;
import targoss.hardcorealchemy.util.MiscVanilla;

public class MessageHearts extends MessageToClient<HardcoreAlchemyTweaks> {
    public NBTTagCompound heartsNBT = new NBTTagCompound();
    
    public MessageHearts() {}
    
    /* NOTE: Not all capability storages support overwriting the existing capability in-place. This one does, though. */
    public MessageHearts(ICapabilityHearts hearts) {
        this.heartsNBT = (NBTTagCompound)ProviderHearts.HEARTS_CAPABILITY.getStorage().writeNBT(ProviderHearts.HEARTS_CAPABILITY, hearts, null);
        // For now, don't sync heart shard unlock progress to the client as it's currently not synced in a reliable way anyway.
        this.heartsNBT.removeTag(StorageHearts.SHARD_PROGRESS);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, heartsNBT);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.heartsNBT = ByteBufUtils.readTag(buf);
    }
    
    public static class ReceiveAction implements Runnable {
        protected NBTTagCompound heartsNBT;
        
        public ReceiveAction(NBTTagCompound heartsNBT) {
            this.heartsNBT = heartsNBT;
        }

        @Override
        public void run() {
            EntityPlayer player = MiscVanilla.getTheMinecraftPlayer();
            ICapabilityHearts hearts = player.getCapability(ProviderHearts.HEARTS_CAPABILITY, null);
            if (hearts == null) {
                return;
            }
            ProviderHearts.HEARTS_CAPABILITY.getStorage().readNBT(ProviderHearts.HEARTS_CAPABILITY, hearts, null, heartsNBT);
            ListenerHearts.updateHeartModifiers(HardcoreAlchemyCore.proxy.configs, player, hearts);
        }
        
    }
    
    public static class Handler implements IMessageHandler<MessageHearts, IMessage> {
        @Override
        public IMessage onMessage(MessageHearts message, MessageContext ctx) {
            message.getThreadListener().addScheduledTask(
                new ReceiveAction(message.heartsNBT)
            );
            return null;
        }
    }

    @Override
    public Class<? extends IMessageHandler<? extends MessageToClient<HardcoreAlchemyTweaks>, IMessage>> getHandlerClass() {
        return Handler.class;
    }

}
