/*
 * Copyright 2017-2023 asanetargoss
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

package targoss.hardcorealchemy.incantation;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import targoss.hardcorealchemy.incantation.api.ISpell;
import targoss.hardcorealchemy.incantation.api.Incantation;

public class IncantationParts implements IMessage {
    public static enum Type {
        FILLER,
        WORD,
        INCANTATION
    }
    
    public static class IncantationDefinition {
        public String displayString;
        public Incantation incantation;
        public ISpell spell;

        public IncantationDefinition(String displayString, Incantation incantation, ISpell spell) {
            this.displayString = displayString;
            this.incantation = incantation;
            this.spell = spell;
        }
    }
    
    protected List<Type> types = new ArrayList<>();

    protected List<String> fillers = new ArrayList<>();
    protected List<String> words = new ArrayList<>();
    protected List<IncantationDefinition> incantations = new ArrayList<>();
    
    public boolean hasIncantation() {
        return !incantations.isEmpty();
    }
    
    public void addFiller(String filler) {
        types.add(Type.FILLER);
        fillers.add(filler);
    }
    
    public void addWord(String word) {
        types.add(Type.WORD);
        words.add(word);
    }
    
    public void addIncantation(IncantationDefinition incantation) {
        types.add(Type.INCANTATION);
        incantations.add(incantation);
    }
    
    public boolean isEmpty() {
        return types.isEmpty();
    }
    
    public void clear() {
        types.clear();
        fillers.clear();
        words.clear();
        incantations.clear();
    }
    
    public class Iterator {
        protected int i = 0;
        protected int iFiller = 0;
        protected int iWord = 0;
        protected int iIncantation = 0;
        
        public boolean hasNext() {
            return i < types.size();
        }
        
        public Type checkNextType() {
            return types.get(i);
        }
        
        public String nextFiller() {
            assert(types.get(i) == Type.FILLER);
            String filler = fillers.get(iFiller);
            ++i;
            ++iFiller;
            return filler;
        }
        
        public String nextWord() {
            assert(types.get(i) == Type.WORD);
            String word = words.get(iWord);
            ++i;
            ++iWord;
            return word;
        }
        
        public IncantationDefinition nextIncantation() {
            assert(types.get(i) == Type.INCANTATION);
            IncantationDefinition incantation = incantations.get(iIncantation);
            ++i;
            ++iIncantation;
            return incantation;
        }
    }
    
    public Iterator iterator() {
        return new Iterator();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(types.size());
        for (Type type : types) {
            buf.writeByte(type.ordinal());
        }
        buf.writeInt(fillers.size());
        for (String filler : fillers) {
            ByteBufUtils.writeUTF8String(buf, filler);
        }
        buf.writeInt(words.size());
        for (String word : words) {
            ByteBufUtils.writeUTF8String(buf, word);
        }
        buf.writeInt(incantations.size());
        for (IncantationDefinition incantation : incantations) {
            ByteBufUtils.writeUTF8String(buf, incantation.displayString);
            ByteBufUtils.writeUTF8String(buf, incantation.incantation.getRegistryName().toString());
            incantation.incantation.spellToBytes(buf, incantation.spell);
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int numTypes = buf.readInt();
        for (int i = 0; i < numTypes; ++i) {
            byte typeByte = buf.readByte();
            Type type = Type.values()[typeByte];
            types.add(type);
        }
        int numFillers = buf.readInt();
        for (int i = 0; i < numFillers; ++i) {
            fillers.add(ByteBufUtils.readUTF8String(buf));
        }
        int numWords = buf.readInt();
        for (int i = 0; i < numWords; ++i) {
            words.add(ByteBufUtils.readUTF8String(buf));
        }
        int numIncantations = buf.readInt();
        for (int i = 0; i < numIncantations; ++i) {
            String displayString = ByteBufUtils.readUTF8String(buf);
            String incantationName = ByteBufUtils.readUTF8String(buf);
            Incantation incantation = Incantations.INCANTATION_REGISTRY.getValue(new ResourceLocation(incantationName));
            ISpell spell = incantation.spellFromBytes(buf);
            incantations.add(new IncantationDefinition(displayString, incantation, spell));
        }
    }
    
    public boolean isValid() {
        int numFillers = 0;
        int numWords = 0;
        int numIncantations = 0;
        for (Type type : types) {
            switch (type) {
            case FILLER:
                numFillers++;
                break;
            case WORD:
                numWords++;
                break;
            case INCANTATION:
                numIncantations++;
                break;
            }
        }
        
        if (numFillers != fillers.size() ||
                numWords != words.size() ||
                numIncantations != incantations.size()) {
            return false;
        }
        
        for (String filler : fillers) {
            if (filler == null) {
                return false;
            }
        }
        
        for (String word : words) {
            if (word == null) {
                return false;
            }
        }
        
        for (IncantationDefinition incantation : incantations) {
            if (incantation.displayString == null ||
                    incantation.incantation == null) {
                return false;
            }
        }
        
        return true;
    }
}
