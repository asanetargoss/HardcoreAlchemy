/*
 * Copyright 2017-2022 asanetargoss
 *
 * This file is part of Hardcore Alchemy Core.
 *
 * Hardcore Alchemy Core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Core is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Core. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.listener;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.network.MessageConfigs;

public class ListenerConfigs extends HardcoreAlchemyListener {
    // TODO: Handle the player logging out from the server
    
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        coreConfigs.init(event.getModConfigurationDirectory().toPath().resolve(HardcoreAlchemy.MOD_ID).toFile());
        coreConfigs.load();
        coreConfigs.save();
    }
    
    /**
     * When the player is about to enter their world, reload configs from disc, in case they were on a server before.
     */
    @Override
    public void serverAboutToStart(FMLServerAboutToStartEvent event) {
        coreConfigs.load();
    }
    
    /**
     * Higher priority so it syncs before capabilities.
     * Minecraft uses TCP, so this always works.
     * */
    @SubscribeEvent(priority=EventPriority.HIGH)
    public void onPlayerJoinMP(EntityJoinWorldEvent event) {
        if (!(event.getEntity() instanceof EntityPlayerMP)) {
            return;
        }
        HardcoreAlchemy.proxy.messenger.sendTo(new MessageConfigs(coreConfigs), (EntityPlayerMP)event.getEntity());
    }
}
