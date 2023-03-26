package targoss.hardcorealchemy.gui;

import targoss.hardcorealchemy.HardcoreAlchemyCore;
import targoss.hardcorealchemy.coremod.HardcoreAlchemyPreInit;
import targoss.hardcorealchemy.registrar.Registrar;
import targoss.hardcorealchemy.registrar.RegistrarGuiHandler;

public class Guis {
    public static final Registrar<IndexedGuiHandler> GUI_HANDLERS = new RegistrarGuiHandler("guis", HardcoreAlchemyCore.MOD_ID, HardcoreAlchemyPreInit.LOGGER);
}
