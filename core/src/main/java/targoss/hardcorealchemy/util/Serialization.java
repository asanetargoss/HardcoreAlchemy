/*
 * Copyright 2019 asanetargoss
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

package targoss.hardcorealchemy.util;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import targoss.hardcorealchemy.ModStateException;

public class Serialization {
    private static final List<String> nbtTypeList = (List<String>)Arrays.asList(NBTBase.NBT_TYPES);
    private static int checkForNBTType(String nbtTypeString) {
        int nbtType = nbtTypeList.indexOf(nbtTypeString);
        if (NBT_COMPOUND_ID == -1) {
            throw new ModStateException(Serialization.class.getSimpleName() + ".NBT_" + nbtTypeString + "_ID could not be defined. Have NBT constants changed?");
        }
        return nbtType;
    }
    
    public static final int NBT_STRING_ID = checkForNBTType("STRING");
    public static final int NBT_LIST_ID = checkForNBTType("LIST");
    public static final int NBT_COMPOUND_ID = checkForNBTType("COMPOUND");
    public static final int NBT_INT_ARRAY_ID = checkForNBTType("INT[]");
    
    public static void setBlockPosNBT(NBTTagCompound parent, String key, @Nullable BlockPos pos) {
        if (pos == null) {
            return;
        }
        NBTTagCompound posNBT = new NBTTagCompound();
        posNBT.setInteger("x", pos.getX());
        posNBT.setInteger("y", pos.getY());
        posNBT.setInteger("z", pos.getZ());
        parent.setTag(key, posNBT);
    }
    
    public static @Nullable BlockPos getBlockPosNBT(NBTTagCompound parent, String key) {
        if (!parent.hasKey(key)) {
            return null;
        }
        NBTTagCompound posNBT = parent.getCompoundTag(key);
        if (posNBT.getSize() == 0) {
            return null;
        }
        
        int x, y, z;
        if (!posNBT.hasKey("x")) {
            return null;
        }
        x = posNBT.getInteger("x");
        if (!posNBT.hasKey("y")) {
            return null;
        }
        y = posNBT.getInteger("y");
        if (!posNBT.hasKey("z")) {
            return null;
        }
        z = posNBT.getInteger("z");
        
        return new BlockPos(x, y, z);
    }
    
    public static BlockPos readBlockPosFromBuf(ByteBuf buf) {
        int x, y, z;
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        return new BlockPos(x, y, z);
    }
    
    public static void writeBlockPosToBuf(ByteBuf buf, BlockPos pos) {
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());
    }
}
