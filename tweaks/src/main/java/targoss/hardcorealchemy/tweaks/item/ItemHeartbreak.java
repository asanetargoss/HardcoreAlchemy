/*
 * Copyright 2017-2026 asanetargoss
 *
 * This file is part of Hardcore Alchemy Tweaks.
 *
 * Hardcore Alchemy Tweaks is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Tweaks is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Tweaks. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.tweaks.item;

import static net.minecraft.init.Blocks.GLASS;

import javax.annotation.Nullable;

import net.minecraft.block.SoundType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import targoss.hardcorealchemy.HardcoreAlchemyCore;
import targoss.hardcorealchemy.heart.Heart;
import targoss.hardcorealchemy.tweaks.capability.hearts.ICapabilityHearts;
import targoss.hardcorealchemy.tweaks.listener.ListenerHearts;

public class ItemHeartbreak extends ItemFood {
    @CapabilityInject(ICapabilityHearts.class)
    public static final Capability<ICapabilityHearts> HEARTS_CAPABILITY = null;
    
    public ItemHeartbreak() {
        super(3, 0.3F, false);
        setAlwaysEdible();
    }
    
    @Override
    protected void onFoodEaten(ItemStack stack, World world, EntityPlayer player) {
        if (!world.isRemote) {
            ICapabilityHearts hearts = player.getCapability(HEARTS_CAPABILITY, null);
            if (hearts != null) {
                @Nullable Heart randomHeart = ListenerHearts.getRandomHeart(hearts);
                if (randomHeart != null) {
                    // Remove shard progress associated with this heart, if available
                    hearts.getShardProgressMap().remove(randomHeart);
                    // This will also sync the heart capability map
                    ListenerHearts.removeHeart(HardcoreAlchemyCore.proxy.configs, player, hearts, randomHeart);
                    // Breaking glass sound
                    BlockPos playerPos = player.getPosition();
                    SoundType soundtype = GLASS.getSoundType(GLASS.getDefaultState(), world, playerPos, null);
                    world.playSound(null, playerPos, soundtype.getBreakSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                }
            }
        }
        
        super.onFoodEaten(stack, world, player);
    }
}
