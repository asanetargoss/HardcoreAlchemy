/*
 * Copyright 2020 asanetargoss
 * 
 * This file is part of Hardcore Alchemy.
 * 
 * Hardcore Alchemy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation version 3 of the License.
 * 
 * Hardcore Alchemy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Hardcore Alchemy.  If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.incantation;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.IForgeRegistry;
import net.minecraftforge.fml.common.registry.RegistryBuilder;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.coremod.HardcoreAlchemyPreInit;
import targoss.hardcorealchemy.incantation.api.Incantation;
import targoss.hardcorealchemy.registrar.Registrar;
import targoss.hardcorealchemy.registrar.RegistrarForge;

public class Incantations {
    public static final Registrar<Incantation> INCANTATIONS = new RegistrarForge<Incantation>("incantations", HardcoreAlchemy.MOD_ID, HardcoreAlchemyPreInit.LOGGER);
    
    public static final IForgeRegistry<Incantation> INCANTATION_REGISTRY = new RegistryBuilder<Incantation>()
            .setName(new ResourceLocation(HardcoreAlchemy.MOD_ID, "incantations"))
            .setType(Incantation.class)
            .setIDRange(0, 1024)
            .create();
}
