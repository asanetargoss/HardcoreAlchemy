package targoss.hardcorealchemy.tweaks.listener;

import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import targoss.hardcorealchemy.listener.HardcoreAlchemyListener;
import targoss.hardcorealchemy.tweaks.item.Items;

public class ListenerClientItems extends HardcoreAlchemyListener {
    @SubscribeEvent
    public void onModelBake(ModelBakeEvent event) {
        Items.ClientSide.onModelBake(event);
    }
}
