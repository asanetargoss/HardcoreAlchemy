package targoss.hardcorealchemy.creatures.listener;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import targoss.hardcorealchemy.creatures.block.TileHeartOfForm;
import targoss.hardcorealchemy.listener.HardcoreAlchemyListener;

public class ListenerBlockHeartOfForm extends HardcoreAlchemyListener {
    @SubscribeEvent
    void onAttachCapability(@SuppressWarnings("deprecation") AttachCapabilitiesEvent.TileEntity event) {
        TileEntity te = event.getObject();
        if (!(te instanceof TileHeartOfForm)) {
            return;
        }
        event.addCapability(TileHeartOfForm.ITEM_HANDLER_RESOURCE, new TileHeartOfForm.ItemHandlerProvider());
    }
}
