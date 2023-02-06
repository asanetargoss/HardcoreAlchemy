package targoss.hardcorealchemy.registrar;

import org.apache.logging.log4j.Logger;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class RegistrarBlock extends RegistrarForge<Block> {

    public RegistrarBlock(String name, String namespace, Logger logger) {
        super(name, namespace, logger);
    }
    
    @Override
    public <V extends Block> V add(String entryName, V entry) {
        V result = super.add(entryName, entry);
        ResourceLocation loc = entry.getRegistryName();
        entry.setUnlocalizedName(loc.getResourceDomain() + "." + loc.getResourcePath());
        return result;
    }
    
    public boolean register() {
        if (!super.register()) {
            return false;
        }
        
        for (Block entry : entries) {
            GameRegistry.register(new ItemBlock(entry), entry.getRegistryName());
        }
        
        return true;
    }
}
