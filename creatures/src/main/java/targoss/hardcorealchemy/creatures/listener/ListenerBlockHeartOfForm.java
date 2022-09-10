/*
 * Copyright 2017-2022 asanetargoss
 *
 * This file is part of Hardcore Alchemy Creatures.
 *
 * Hardcore Alchemy Creatures is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Creatures is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Creatures. If not, see <http://www.gnu.org/licenses/>.
 */

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
