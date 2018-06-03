/*
 * Copyright 2018 asanetargoss
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

package targoss.hardcorealchemy.instinct;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.IForgeRegistry;
import net.minecraftforge.fml.common.registry.IForgeRegistryEntry;
import net.minecraftforge.fml.common.registry.RegistryBuilder;
import targoss.hardcorealchemy.HardcoreAlchemy;

public class Instincts {
    private static List<InstinctFactory> INSTINCT_FACTORY_CACHE = new ArrayList<>();
    
    public static final IForgeRegistry<InstinctFactory> REGISTRY = new RegistryBuilder<InstinctFactory>()
            .setName(new ResourceLocation(HardcoreAlchemy.MOD_ID, "instinct_factories"))
            .setType(InstinctFactory.class)
            .setIDRange(0, 1024)
            .create();
    
    public static final InstinctFactory ATTACK_PREY_ONLY = instinct("attack_prey_only", InstinctAttackPreyOnly.class);
    
    public static class InstinctFactory extends IForgeRegistryEntry.Impl<InstinctFactory> {
        public final Class<? extends IInstinct> instinctClass;
        public final IInstinct instinctObject;
        
        public InstinctFactory(Class<? extends IInstinct> instinctClass) {
            this.instinctClass = instinctClass;
            this.instinctObject = createInstinct();
        }
        
        public IInstinct createInstinct() {
            try {
                return instinctClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                HardcoreAlchemy.LOGGER.error("The instinct '" + REGISTRY.getKey(this).toString() +
                        "' could not be created properly. Did the developer forget to define the default constructor?");
                e.printStackTrace();
            }
            return null;
        }
        
        public IInstinct createInstinct(EntityLivingBase morphEntity) {
            return instinctObject.createInstanceFromMorphEntity(morphEntity);
        }
    }
    
    private static InstinctFactory instinct(String name, Class<? extends IInstinct> instinctClass) {
        InstinctFactory instinctEntry = new InstinctFactory(instinctClass);
        instinctEntry.setRegistryName(new ResourceLocation(HardcoreAlchemy.MOD_ID, name));
        INSTINCT_FACTORY_CACHE.add(instinctEntry);
        return instinctEntry;
    }
    
    public static void registerInstincts() {
        for (InstinctFactory instinctEntry : INSTINCT_FACTORY_CACHE) {
            GameRegistry.register(instinctEntry);
        }
        INSTINCT_FACTORY_CACHE.clear();
    }
}
