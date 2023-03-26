package targoss.hardcorealchemy.creatures.gui;

import static targoss.hardcorealchemy.gui.Guis.GUI_HANDLERS;

import targoss.hardcorealchemy.gui.IndexedGuiHandler;

public class Guis {
    public static final IndexedGuiHandler GUI_HANDLER_HUMANITY_PHYLACTERY = GUI_HANDLERS.add("humanity_phylactery", new IndexedGuiHandler(new GuiHandlerHumanityPhylactery()));
}
