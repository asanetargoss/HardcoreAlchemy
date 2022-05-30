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

package targoss.hardcorealchemy.incantation;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.util.text.ITextComponent;
import targoss.hardcorealchemy.incantation.api.Incantation;
import targoss.hardcorealchemy.util.MiscVanilla;

public class IncantationLookup {
    protected String currentLocale = "";
    protected Map<String, Incantation> map = new HashMap<String, Incantation>();

    /**
     * This should be called if resources are reloaded, to force initialization
     */
    public void clear() {
        currentLocale = "";
    }
    
    protected void init() {
        map.clear();
        for (Incantation incantation : Incantations.INCANTATION_REGISTRY.getValues()) {
            ITextComponent command = incantation.getCommand();
            String word = command.getUnformattedText();
            map.put(word, incantation);
        }
    }
    
    public @Nullable Incantation get(String word) {
        String expectedLocale = MiscVanilla.getCurrentLocale();
        if (expectedLocale != currentLocale) {
            init();
            currentLocale = expectedLocale;
        }
        return map.get(word);
    }
}
