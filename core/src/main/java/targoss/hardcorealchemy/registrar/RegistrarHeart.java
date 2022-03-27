package targoss.hardcorealchemy.registrar;

import java.util.List;

import org.apache.logging.log4j.Logger;

import targoss.hardcorealchemy.heart.Heart;

public class RegistrarHeart extends RegistrarForge<Heart> {
    public RegistrarHeart(String name, String namespace, Logger logger) {
        super(name, namespace, logger);
    }
    
    @Override
    public <V extends Heart> V add(String entryName, V entry) {
        entry = super.add(entryName, entry);
        entry.name = entryName;
        return entry;
    }
    
    public static class RegistryBase {
        public boolean register(List<Heart> entries) {
            return false;
        }
    }
    
    public RegistryBase IMPL = new RegistryBase();
    
    @Override
    public boolean register() {
        if (!super.register()) {
            return false;
        }
        return IMPL.register(entries);
    }
}
