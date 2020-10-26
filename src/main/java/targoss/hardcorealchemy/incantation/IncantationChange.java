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

package targoss.hardcorealchemy.incantation;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import targoss.hardcorealchemy.incantation.api.IIncantationParser;
import targoss.hardcorealchemy.incantation.api.ISpell;
import targoss.hardcorealchemy.incantation.api.Incantation;

public class IncantationChange extends Incantation {

    @Override
    public ITextComponent getName() {
        return new TextComponentTranslation("hardcorealchemy.incantation.change.name");
    }

    @Override
    public ITextComponent getCommand() {
        return new TextComponentTranslation("hardcorealchemy.incantation.change.command");
    }

    @Override
    public ISpell getSpell(IIncantationParser parser) {
        return new SpellChange();
    }

    @Override
    public void spellToBytes(ByteBuf buf, ISpell spell) {}

    @Override
    public ISpell spellFromBytes(ByteBuf buf) {
        return new SpellChange();
    }

}
