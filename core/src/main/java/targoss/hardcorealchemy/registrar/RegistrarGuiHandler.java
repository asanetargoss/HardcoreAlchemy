package targoss.hardcorealchemy.registrar;

import org.apache.logging.log4j.Logger;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import targoss.hardcorealchemy.gui.IndexedGuiHandler;

public class RegistrarGuiHandler extends Registrar<IndexedGuiHandler> {
    public RegistrarGuiHandler(String name, String namespace, Logger logger) {
        super(name, namespace, logger);
    }
    
    public <V extends IndexedGuiHandler> V add(String entryName, V entry) {
        entry = super.add(entryName, entry);
        entry.index = entries.size() - 1;
        entry.id = new ResourceLocation(namespace, entryName);
        return entry;
    }
    
    public class GuiHandler implements IGuiHandler {
        @Override
        public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
            if (ID > entries.size() - 1) {
                return null;
            }
            return entries.get(ID).handler.getServerGuiElement(ID, player, world, x, y, z);
        }

        @Override
        public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
            return entries.get(ID).handler.getClientGuiElement(ID, player, world, x, y, z);
        }
    }
    
    public final GuiHandler guiHandler = new GuiHandler();
    
    @Override
    public boolean register(int phase) {
        if (!super.register(phase)) {
            return false;
        }
        if (phase != 0) {
            return false;
        }
        NetworkRegistry.INSTANCE.registerGuiHandler(namespace, guiHandler);
        return true;
    }
}
