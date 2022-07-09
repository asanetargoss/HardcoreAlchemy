/*
 * Copyright 2017-2022 asanetargoss
 *
 * This file is part of Hardcore Alchemy Core.
 *
 * Hardcore Alchemy Core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Core is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Core. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import targoss.hardcorealchemy.HardcoreAlchemyCore;
import targoss.hardcorealchemy.util.MiscVanilla;

public class MessageFakeBlockBreak extends MessageToClient<HardcoreAlchemyCore> {
    public int blockID;
    public int stateID;
    public int x;
    public int y;
    public int z;

    public MessageFakeBlockBreak() {}
    
    public MessageFakeBlockBreak(int blockID, int stateID, int x, int y, int z) {
        this.blockID = blockID;
        this.stateID = stateID;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(blockID);
        buf.writeInt(stateID);
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        blockID = buf.readInt();
        stateID = buf.readInt();
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
    }
    
    public static class ReceiveAction implements Runnable {
        protected int blockID;
        protected int stateID;
        protected int x;
        protected int y;
        protected int z;
        
        public ReceiveAction(int blockID, int stateID, int x, int y, int z) {
            this.blockID = blockID;
            this.stateID = stateID;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @SuppressWarnings("deprecation")
        @Override
        public void run() {
            EntityPlayer player = MiscVanilla.getTheMinecraftPlayer();
            World world = player.world;
            assert(world instanceof WorldClient);
            WorldClient worldClient = (WorldClient)world;
            BlockPos blockPos = new BlockPos(x, y, z);
            
            // Adapted from RenderGlobal.playEvent (1.10)
            Block block = Block.getBlockById(stateID & 4095);
            if (block.getDefaultState().getMaterial() != Material.AIR) {
                SoundType soundType = block.getSoundType(Block.getStateById(stateID), world, blockPos, null);
                worldClient.playSound(blockPos, soundType.getBreakSound(), SoundCategory.BLOCKS, (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F, false);
            }
            Minecraft.getMinecraft().effectRenderer.addBlockDestroyEffects(blockPos, block.getStateFromMeta(stateID >> 12 & 255));
        }
        
    }
    
    public static class Handler implements IMessageHandler<MessageFakeBlockBreak, IMessage> {

        @Override
        public IMessage onMessage(MessageFakeBlockBreak message, MessageContext ctx) {
            message.getThreadListener().addScheduledTask(new ReceiveAction(message.blockID, message.stateID, message.x, message.y, message.z));
            return null;
        }
        
    }

    @Override
    public Class<? extends IMessageHandler<? extends MessageToClient<HardcoreAlchemyCore>, IMessage>> getHandlerClass() {
        return Handler.class;
    }

}
