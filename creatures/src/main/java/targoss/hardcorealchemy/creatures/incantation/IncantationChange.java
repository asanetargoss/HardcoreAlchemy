/*
 * Copyright 2017-2023 asanetargoss
 *
 * This file is part of Hardcore Alchemy Creatures.
 *
 * Hardcore Alchemy Creatures is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Creatures is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Creatures. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.creatures.incantation;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import targoss.hardcorealchemy.HardcoreAlchemyCore;
import targoss.hardcorealchemy.incantation.api.IIncantationParser;
import targoss.hardcorealchemy.incantation.api.ISpell;
import targoss.hardcorealchemy.incantation.api.Incantation;

public class IncantationChange extends Incantation {
    protected final ITextComponent NAME = new TextComponentTranslation(HardcoreAlchemyCore.MOD_ID + ".incantation.change.name");
    protected final ITextComponent COMMAND = new TextComponentTranslation(HardcoreAlchemyCore.MOD_ID + ".incantation.change.command");

    @Override
    public ITextComponent getName() {
        return NAME;
    }

    @Override
    public ITextComponent getCommand() {
        return COMMAND;
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
