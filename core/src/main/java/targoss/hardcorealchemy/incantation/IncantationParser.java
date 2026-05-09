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

import targoss.hardcorealchemy.incantation.IncantationParts.IncantationDefinition;
import targoss.hardcorealchemy.incantation.api.IIncantationParser;
import targoss.hardcorealchemy.incantation.api.ISpell;

public class IncantationParser implements IIncantationParser {

    public static final char[] fillerCharacters = new char[]{
        ' ',
        '\t',
        '\n',
        ',',
        ';',
        '.',
        '\'',
        '!',
        '?'
    };

    public static final IncantationLookup INCANTATION_LOOKUP = new IncantationLookup();

    public static IncantationParts preParseIncantation(String message) {
        IncantationParts parts = new IncantationParts();
        
        String piece = "";
        IncantationParts.Type type = IncantationParts.Type.FILLER;
        for (int i = 0; i < message.length(); ++i) {
            char c = message.charAt(i);
            IncantationParts.Type nextType = IncantationParts.Type.WORD;
            for (int j = 0; j < IncantationParser.fillerCharacters.length; ++j) {
                if (IncantationParser.fillerCharacters[j] == c) {
                    nextType = IncantationParts.Type.FILLER;
                    break;
                }
            }
            
            if (nextType != type && !piece.isEmpty()) {
                switch (type) {
                case FILLER:
                    parts.addFiller(piece);
                    break;
                case WORD:
                    parts.addWord(piece);
                    break;
                case INCANTATION:
                    break;
                }
                piece = "";
            }
            
            type = nextType;
            piece += c;
        }
        
        if (!piece.isEmpty()) {
            switch (type) {
            case FILLER:
                parts.addFiller(piece);
                break;
            case WORD:
                parts.addWord(piece);
                break;
            case INCANTATION:
                break;
            }
            piece = "";
        }
        
        return parts;
    }

    public static IncantationParts parseIncantations(String message) {
        IncantationParts parts = new IncantationParts();
        if (message.isEmpty()) {
            return parts;
        }
        IncantationParser parser = new IncantationParser();
        int i = 0;
        int lastStart = 0;
        while (i < message.length()) {
            IncantationLookup.Node.Result result = IncantationParser.INCANTATION_LOOKUP.find(message, i);
            if (result == null) {
                ++i;
            } else {
                // Add words and filler before the incantation, if present
                if (lastStart < i) {
                    String subMessage = message.substring(lastStart, i);
                    IncantationParts wordsAndFillers = preParseIncantation(subMessage);
                    parts.add(wordsAndFillers);
                }
                String incantationString = message.substring(i, result.end);
                if (result.incantation == null) {
                    assert(false);
                    parts.addWord(incantationString);
                } else {
                    ISpell spell = result.incantation.getSpell(parser);
                    IncantationDefinition def = new IncantationDefinition(incantationString, result.incantation, spell);
                    parts.addIncantation(def);
                }
                i = lastStart = result.end;
            }
        }
        // Add words and filler after the last incantation, or the start if there was no incantation
        if (lastStart < i || lastStart == 0) {
            String subMessage = message.substring(lastStart, i);
            IncantationParts wordsAndFillers = preParseIncantation(subMessage);
            parts.add(wordsAndFillers);
        }
        return parts;
    }

}
