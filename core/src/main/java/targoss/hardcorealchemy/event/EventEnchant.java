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

package targoss.hardcorealchemy.event;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;

// TODO: Use this event
public class EventEnchant extends Event {
    public static class Post extends EventEnchant {
        public final EntityPlayer player;
        public ItemStack enchantStack;

        public Post(EntityPlayer player, ItemStack enchantStack) {
            this.player = player;
            this.enchantStack = enchantStack;
        }
    }
    
    public static @Nonnull ItemStack onEnchantPost(EntityPlayer player, @Nonnull ItemStack enchantStack) {
        EventEnchant.Post event =  new EventEnchant.Post(player, enchantStack);
        MinecraftForge.EVENT_BUS.post(event);
        return event.enchantStack;
    }
}
