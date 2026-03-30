/*
 * Copyright 2017-2026 asanetargoss
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

import org.apache.logging.log4j.Level;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import targoss.hardcorealchemy.HardcoreAlchemyCore;
import targoss.hardcorealchemy.incantation.api.ISpell;
import targoss.hardcorealchemy.incantation.api.Incantation;
import targoss.hardcorealchemy.util.Chat;
import targoss.hardcorealchemy.util.StringUtil;

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
    
    @Deprecated
    public List<Type> types = new ArrayList<>();

    @Deprecated
    public List<String> fillers = new ArrayList<>();
    @Deprecated
    public List<String> words = new ArrayList<>();
    @Deprecated
    public List<IncantationDefinition> incantations = new ArrayList<>();
    
    public String toString() {
        String ret = "{types:[";
        boolean firstType = true;
        for (Type type : types) {
            if (!firstType) {
                ret += ",";
            }
            firstType = false;
            ret += type.toString();
        }
        ret += "],fillers:[";
        boolean firstFiller = true;
        for (String filler : fillers) {
            if (!firstFiller) {
                ret += ",";
            }
            firstFiller = false;
            ret += "\"" + filler.replaceAll("\"", "\\\"") + "\"";
        }
        ret += "],words:[";
        boolean firstWord = true;
        for (String word : words) {
            if (!firstWord) {
                ret += ",";
            }
            firstWord = false;
            ret += "\"" + word.replaceAll("\"", "\\\"") + "\"";
        }
        ret += "],incantations:[";
        boolean firstIncantation = true;
        for (IncantationDefinition incantation : incantations) {
            if (!firstIncantation) {
                ret += ",";
            }
            firstIncantation = false;
            ret += "\"" + incantation.displayString.replaceAll("\"",  "\\\"") + "\"<" + incantation.incantation.getRegistryName().toString() + ">";
        }
        ret += "])";
        return ret;
    }
    
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
    
    public class ReverseIterator {
        protected int i = types.size() - 1;
        protected int iFiller = fillers.size() - 1;
        protected int iWord = words.size() - 1;
        protected int iIncantation = incantations.size() - 1;
        
        public boolean hasNext() {
            return i >= 0;
        }
        
        public Type checkNextType() {
            return types.get(i);
        }
        
        public String nextFiller() {
            assert(types.get(i) == Type.FILLER);
            String filler = fillers.get(iFiller);
            --i;
            --iFiller;
            return filler;
        }
        
        public String nextWord() {
            assert(types.get(i) == Type.WORD);
            String word = words.get(iWord);
            --i;
            --iWord;
            return word;
        }
        
        public IncantationDefinition nextIncantation() {
            assert(types.get(i) == Type.INCANTATION);
            IncantationDefinition incantation = incantations.get(iIncantation);
            --i;
            --iIncantation;
            return incantation;
        }
    }
    
    public ReverseIterator reverseIterator() {
        return new ReverseIterator();
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
        try {
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
                if (incantation == null) {
                    throw new NullPointerException("Invalid incantation resource location: \"" + incantationName + "\"");
                }
                ISpell spell = incantation.spellFromBytes(buf);
                if (spell == null) {
                    throw new NullPointerException("Invalid spell generated for incantation: \"" + incantationName + "\"");
                }
                incantations.add(new IncantationDefinition(displayString, incantation, spell));
            }
        } catch (Exception e) {
            HardcoreAlchemyCore.LOGGER.catching(Level.DEBUG, e);
            clear();
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
                numIncantations != incantations.size() ||
                numIncantations == 0) {
            return false;
        }
        
        for (String filler : fillers) {
            if (filler == null || !Chat.isAllowedCharacters(filler) || filler.isEmpty()) {
                return false;
            }
        }
        
        for (String word : words) {
            if (word == null || !Chat.isAllowedCharacters(word) || word.isEmpty()) {
                return false;
            }
        }
        
        for (IncantationDefinition incantation : incantations) {
            if (incantation.displayString == null ||
                    incantation.incantation == null ||
                    !Chat.isAllowedCharacters(incantation.displayString) ||
                    incantation.displayString.isEmpty()) {
                return false;
            }
        }
        
        return true;
    }
    
    public void trim() {
        int leadingFillers = 0;
        int leadingWords = 0;
        int leadingIncantations = 0;
        int trailingFillers = 0;
        int trailingWords = 0;
        int trailingIncantations = 0;
        {
            Iterator leadingIt = iterator();
            leading:
            while (leadingIt.hasNext()) {
                Type type = leadingIt.checkNextType();
                switch (type) {
                case FILLER:
                    String filler = leadingIt.nextFiller();
                    filler = StringUtil.trimBefore(filler);
                    fillers.set(leadingIt.iFiller - 1, filler);
                    if (filler.length() == 0) {
                        ++leadingFillers;
                    } else {
                        break leading;
                    }
                    break;
                case WORD:
                    String word = leadingIt.nextWord();
                    word = StringUtil.trimBefore(word);
                    words.set(leadingIt.iWord - 1, word);
                    if (word.length() == 0) {
                        ++leadingWords;
                    } else {
                        break leading;
                    }
                    break;
                case INCANTATION:
                    IncantationDefinition incantation = leadingIt.nextIncantation();
                    incantation.displayString = StringUtil.trimBefore(incantation.displayString);
                    if (incantation.displayString.length() == 0) {
                        ++leadingIncantations;
                    } else {
                        break leading;
                    }
                    break;
                default:
                    break leading;
                }
            }
        }
        
        {
            ReverseIterator trailingIt = reverseIterator();
            trailing:
            while (trailingIt.hasNext()) {
                Type type = trailingIt.checkNextType();
                switch (type) {
                case FILLER:
                    String filler = trailingIt.nextFiller();
                    filler = StringUtil.trimAfter(filler);
                    fillers.set(trailingIt.iFiller + 1, filler);
                    if (filler.length() == 0) {
                        ++trailingFillers;
                    } else {
                        break trailing;
                    }
                    break;
                case WORD:
                    String word = trailingIt.nextWord();
                    word = StringUtil.trimAfter(word);
                    words.set(trailingIt.iWord + 1, word);
                    if (word.length() == 0) {
                        ++trailingWords;
                    } else {
                        break trailing;
                    }
                    break;
                case INCANTATION:
                    IncantationDefinition incantation = trailingIt.nextIncantation();
                    incantation.displayString = StringUtil.trimAfter(incantation.displayString);
                    if (incantation.displayString.length() == 0) {
                        ++trailingIncantations;
                    } else {
                        break trailing;
                    }
                    break;
                default:
                    break trailing;
                }
            }
        }
        
        int leading = leadingFillers + leadingWords + leadingIncantations;
        if ((leadingFillers + leadingWords) == types.size()) {
            assert(false); // Should have called isValid() first
            clear();
        } else {
            int trailing = trailingFillers + trailingWords + trailingIncantations;
            types = types.subList(leading, types.size() - trailing);
            fillers = fillers.subList(leadingFillers, fillers.size() - trailingFillers);
            words = words.subList(leadingWords, words.size() - trailingWords);
            incantations = incantations.subList(leadingIncantations, incantations.size() - trailingIncantations);
        }
        
    }
}
