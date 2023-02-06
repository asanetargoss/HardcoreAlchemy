package targoss.hardcorealchemy.registrar;

import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.common.registry.GameRegistry;
import targoss.hardcorealchemy.block.TileEntityInfo;

public class RegistrarTileEntity extends Registrar<TileEntityInfo<?>> {

    public RegistrarTileEntity(String name, String namespace, Logger logger) {
        super(name, namespace, logger);
    }
    
    @Override
    public <V extends TileEntityInfo<?>> V add(String entryName, V entry) {
        V result = super.add(entryName, entry);
        result.name = namespace + "_" + entryName;
        return result;
    }
    
    public boolean register() {
        if (!super.register()) {
            return false;
        }
        
        for (TileEntityInfo<?> entry : entries) {
            GameRegistry.registerTileEntity(entry.clazz, entry.name);
        }
        
        return true;
    }
}
