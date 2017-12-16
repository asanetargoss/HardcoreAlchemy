package targoss.hardcorealchemy.listener;

import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.config.Configs;

/**
 * A listener contains Forge events and gameplay logic.
 * It is initialized with a Configs instance.
 */
public abstract class ConfiguredListener {
    private Configs configs;
    
    private ConfiguredListener() { }
    
    public ConfiguredListener(Configs configs) {
        this.configs = configs;
    }
}
