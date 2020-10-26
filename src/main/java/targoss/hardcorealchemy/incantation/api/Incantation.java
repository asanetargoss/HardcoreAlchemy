/*
 * Copyright 2020 asanetargoss
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

package targoss.hardcorealchemy.incantation.api;

import javax.annotation.Nullable;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.common.registry.IForgeRegistryEntry;

public abstract class Incantation extends IForgeRegistryEntry.Impl<Incantation> {
    /**
     * Gets the display name of the incantation in the form: "Uppercase"
     */
    public abstract ITextComponent getName();
    
    /**
     * Gets the incantation as used by the player, in the form: "ALLCAPS".
     * Do not use punctuation and whitespace characters.
     * For languages without capital letters, the general rule is to have
     * the player type the word in such a way that it won't be typed by accident.
     */
    public abstract ITextComponent getCommand();
    
    /**
     * Gets the spell that this incantation produces.
     * This is called on the client side using the client's current locale.
     */
    public abstract @Nullable ISpell getSpell(IIncantationParser parser);
    
    public abstract void spellToBytes(ByteBuf buf, ISpell spell);
    
    public abstract ISpell spellFromBytes(ByteBuf buf);
}
