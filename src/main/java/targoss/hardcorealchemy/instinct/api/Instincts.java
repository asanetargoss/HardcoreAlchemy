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

package targoss.hardcorealchemy.instinct.api;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.IForgeRegistry;
import net.minecraftforge.fml.common.registry.RegistryBuilder;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.instinct.InstinctEffectHinderedMind;
import targoss.hardcorealchemy.instinct.InstinctNeedAttackPrey;
import targoss.hardcorealchemy.instinct.InstinctPredator;

public class Instincts {
    private static List<Instinct> INSTINCT_CACHE = new ArrayList<>();

    private static List<InstinctNeedFactory> INSTINCT_NEED_FACTORY_CACHE = new ArrayList<>();

    private static List<InstinctEffect> INSTINCT_EFFECT_CACHE = new ArrayList<>();

    public static final IForgeRegistry<Instinct> REGISTRY = new RegistryBuilder<Instinct>()
            .setName(new ResourceLocation(HardcoreAlchemy.MOD_ID, "instincts"))
            .setType(Instinct.class)
            .setIDRange(0, 1024)
            .create();
    
    public static final IForgeRegistry<InstinctNeedFactory> NEED_FACTORY_REGISTRY = new RegistryBuilder<InstinctNeedFactory>()
            .setName(new ResourceLocation(HardcoreAlchemy.MOD_ID, "instinct_factories"))
            .setType(InstinctNeedFactory.class)
            .setIDRange(0, 1024)
            .create();
    
    public static final IForgeRegistry<InstinctEffect> EFFECT_REGISTRY = new RegistryBuilder<InstinctEffect>()
            .setName(new ResourceLocation(HardcoreAlchemy.MOD_ID, "effects"))
            .setType(InstinctEffect.class)
            .setIDRange(0, 1024)
            .create();

    public static final Instinct PREDATOR = instinct("predator", new InstinctPredator());
    public static final InstinctNeedFactory NEED_ATTACK_PREY = instinctNeed("attack_prey", InstinctNeedAttackPrey.class);
    
    public static final InstinctEffect EFFECT_HINDERED_MIND = instinctEffect("hindered_mind", new InstinctEffectHinderedMind());
    
    private static Instinct instinct(String name, Instinct instinct) {
        instinct.setRegistryName(new ResourceLocation(HardcoreAlchemy.MOD_ID, name));
        INSTINCT_CACHE.add(instinct);
        return instinct;
    }
    
    public static void registerInstincts() {
        for (Instinct instinct : INSTINCT_CACHE) {
            GameRegistry.register(instinct);
        }
        INSTINCT_CACHE.clear();
    }
    
    private static InstinctNeedFactory instinctNeed(String name, Class<? extends IInstinctNeed> instinctClass) {
        InstinctNeedFactory instinctEntry = new InstinctNeedFactory(instinctClass);
        instinctEntry.setRegistryName(new ResourceLocation(HardcoreAlchemy.MOD_ID, name));
        INSTINCT_NEED_FACTORY_CACHE.add(instinctEntry);
        return instinctEntry;
    }
    
    public static void registerInstinctNeeds() {
        for (InstinctNeedFactory instinctEntry : INSTINCT_NEED_FACTORY_CACHE) {
            GameRegistry.register(instinctEntry);
        }
        INSTINCT_NEED_FACTORY_CACHE.clear();
    }
    
    private static InstinctEffect instinctEffect(String name, InstinctEffect effect) {
        effect.setRegistryName(new ResourceLocation(HardcoreAlchemy.MOD_ID, name));
        INSTINCT_EFFECT_CACHE.add(effect);
        return effect;
    }
    
    public static void registerEffects() {
        for (InstinctEffect effect : INSTINCT_EFFECT_CACHE) {
            GameRegistry.register(effect);
        }
        INSTINCT_EFFECT_CACHE.clear();
    }
}
