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

package targoss.hardcorealchemy.creatures.gui;

import static targoss.hardcorealchemy.gui.Guis.GUI_HANDLERS;

import targoss.hardcorealchemy.gui.IndexedGuiHandler;

public class Guis {
    public static final IndexedGuiHandler GUI_HANDLER_HUMANITY_PHYLACTERY = GUI_HANDLERS.add("alchemist_core", new IndexedGuiHandler(new GuiHandlerHumanityPhylactery()));
}
