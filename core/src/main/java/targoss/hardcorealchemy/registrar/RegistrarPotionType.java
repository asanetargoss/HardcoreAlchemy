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

package targoss.hardcorealchemy.registrar;

import org.apache.logging.log4j.Logger;

import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;

public class RegistrarPotionType extends RegistrarForge<PotionType> {
    public RegistrarPotionType(String name, String namespace, Logger logger) {
        super(name, namespace, logger);
    }
    
    public static PotionType potionTypeFromPotion(Potion potion, int duration) {
        PotionType potionType = new PotionType(potion.getRegistryName().toString(), new PotionEffect(potion, duration));
        return potionType;
    }
}
