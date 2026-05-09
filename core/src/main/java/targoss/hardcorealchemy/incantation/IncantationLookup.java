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

import javax.annotation.Nullable;

import net.minecraft.util.text.ITextComponent;
import targoss.hardcorealchemy.incantation.api.Incantation;
import targoss.hardcorealchemy.util.MiscVanilla;

public class IncantationLookup {
    protected String currentLocale = "";
    protected Node root = new Node();

    /**
     * This should be called if resources are reloaded, to force initialization
     */
    public void invalidate() {
        currentLocale = "";
    }
    
    protected void init() {
        root = new Node();
        for (Incantation incantation : Incantations.INCANTATION_REGISTRY.getValues()) {
            root.add(incantation, null);
        }
    }
    
    protected void maybeInit() {
        String expectedLocale = MiscVanilla.getCurrentLocale();
        if (expectedLocale != currentLocale) {
            init();
            currentLocale = expectedLocale;
        }
    }
    
    public @Nullable Node.Result find(String message, int start) {
        maybeInit();
        return root.find(message, start);
    }
    
    /**
     * Incantation matching tree for searching for an incantation of arbitrary complexity and size in a string.
     * Currently, pattern sizes resemble preParseIncantations, but this may change in the future.
     * Currently, patterns are only strings of filler and non-filler, but this may change in the future.
     */
    public static class Node {
        protected String pattern; // null for root or filler
        protected List<Node> children; // null for leaf
        protected Incantation incantation; // non-null for leaf (unless empty)
        
        public void add(Incantation incantation, @Nullable IncantationParts.Iterator it) {
            if (it == null) {
                ITextComponent command = incantation.getCommand();
                String text = command.getUnformattedText();
                IncantationParts parts = IncantationParser.preParseIncantation(text);
                it = parts.iterator();
            }
            assert(it.hasNext());
            IncantationParts.Type type = it.checkNextType();
            assert(type != IncantationParts.Type.INCANTATION);
            String pattern = null;
            switch (type) {
            case FILLER:
                it.nextFiller();
                break;
            case WORD:
                pattern = it.nextWord();
                break;
            case INCANTATION:
            default:
                return;
            }
            Node child = null;
            if (children == null) {
                children = new ArrayList<>();
            }
            for (Node existingChild : children) {
                if (existingChild.pattern == null ? (pattern == null) : existingChild.pattern.equals(pattern)) {
                    child = existingChild;
                    break;
                }
            }
            if (child == null) {
                child = new Node();
                child.pattern = pattern;
                children.add(child);
            }
            if (!it.hasNext()) {
                assert(child.incantation == null);
                child.incantation = incantation;
                return;
            }
            child.add(incantation, it);
        }
        
        public static class Result {
            public int end;
            public Incantation incantation;
            public Result(int end, Incantation incantation) {
                this.end = end;
                this.incantation = incantation;
            }
        }
        
        public @Nullable Result find(String s, int start) {
            if (children == null) {
                // Empty root
                return null;
            }
            int i = start;
            char c = s.charAt(i);
            for (int j = 0; j < IncantationParser.fillerCharacters.length; ++j) {
                if (IncantationParser.fillerCharacters[j] == c) {
                    return null;
                }
            }
            return findAssumeNoLeadingFiller(s, start);
        }
        
        protected @Nullable Result findAssumeNoLeadingFiller(String s, int start) {
            int i = start;
            charIter:
            for (; i < s.length(); ++i) {
                char c = s.charAt(i);
                if (pattern == null) {
                    // Optional filler
                    for (int j = 0; j < IncantationParser.fillerCharacters.length; ++j) {
                        if (IncantationParser.fillerCharacters[j] == c) {
                            continue charIter;
                        }
                    }
                } else {
                    // Required matching character
                    int j = i - start;
                    if (j < pattern.length()) {
                        char p = pattern.charAt(j);
                        if (p != c) {
                            return null;
                        } else {
                            continue charIter;
                        }
                    }
                }
                break;
            }
            // Filler or word match complete. Continue match to child if available.
            if (incantation != null) {
                if (i <= s.length()) {
                    return new Result(i, incantation);
                } else {
                    assert(false);
                    return null;
                }
            }
            if (children == null) {
                assert(false);
                return null;
            }
            for (Node child : children) {
                Result res = child.findAssumeNoLeadingFiller(s, i);
                if (res != null) {
                    return res;
                }
            }
            return null;
        }
    }
}
