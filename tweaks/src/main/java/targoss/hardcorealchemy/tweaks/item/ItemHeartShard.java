/*
 * Copyright 2017-2022 asanetargoss
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

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import targoss.hardcorealchemy.HardcoreAlchemyCore;
import targoss.hardcorealchemy.heart.Heart;

public class ItemHeartShard extends Item {
    public final Heart heart;
    
    public ItemHeartShard(Heart heart) {
        super();
        this.heart = heart;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean advanced)
    {
        String heartNamespace = heart.getRegistryName().getResourceDomain();
        String heartId = heart.getRegistryName().getResourcePath();
        String tooltipKey = heartNamespace + ".heart." + heartId + ".hint";
        ITextComponent tooltipComponent = new TextComponentTranslation(tooltipKey);
        String tooltipLine = tooltipComponent.getFormattedText();
        tooltip.add(tooltipLine);
        
        {
            String generalHintKey1 = HardcoreAlchemyCore.MOD_ID + ".heart.generic_hint.1";
            ITextComponent generalHintComponent1 = new TextComponentTranslation(generalHintKey1);
            String generalHintLine1 = generalHintComponent1.getFormattedText();
            tooltip.add(generalHintLine1);
        }
        {
            String generalHintKey2 = HardcoreAlchemyCore.MOD_ID + ".heart.generic_hint.2";
            ITextComponent generalHintComponent2 = new TextComponentTranslation(generalHintKey2);
            String generalHintLine2 = generalHintComponent2.getFormattedText();
            tooltip.add(generalHintLine2);
        }
    }
}
