package targoss.hardcorealchemy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraftforge.common.MinecraftForge;
import targoss.hardcorealchemy.listener.ConfiguredListener;
import targoss.hardcorealchemy.listener.ListenerGuiHud;
import targoss.hardcorealchemy.listener.ListenerGuiInventory;

public class ClientProxy extends CommonProxy {
    public static final ImmutableList<Class<? extends ConfiguredListener>> LISTENER_TYPES = ImmutableList.of(
            ListenerGuiHud.class,
            ListenerGuiInventory.class
        );
    
    @Override
    public ImmutableList<Class<? extends ConfiguredListener>> getListenerTypes() {
        List<Class<? extends ConfiguredListener>> listenerTypes = new ArrayList<Class<? extends ConfiguredListener>>();
        listenerTypes.addAll(super.getListenerTypes());
        listenerTypes.addAll(LISTENER_TYPES);
        
        return ImmutableList.copyOf(listenerTypes);
    }
}
