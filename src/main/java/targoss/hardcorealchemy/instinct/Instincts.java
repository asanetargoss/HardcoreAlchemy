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

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.IForgeRegistry;
import net.minecraftforge.fml.common.registry.RegistryBuilder;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.instinct.api.IInstinctNeed;
import targoss.hardcorealchemy.instinct.api.Instinct;
import targoss.hardcorealchemy.instinct.api.InstinctEffect;
import targoss.hardcorealchemy.instinct.api.InstinctNeedFactory;
import targoss.hardcorealchemy.instinct.api.InstinctNeedFactorySimple;

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
    public static final Instinct HOMESICK_NATURE = instinct("homesick_nature", new InstinctHomesickNature());
    
    public static final InstinctNeedFactory NEED_ATTACK_PREY = instinctNeed("attack_prey", new InstinctNeedAttackPrey());
    public static final InstinctNeedFactory NEED_SPAWN_ENVIRONMENT = instinctNeed("environment", new IInstinctNeedEnvironment.Factory());
    
    public static final InstinctEffect EFFECT_HINDERED_MIND = instinctEffect("hindered_mind", new InstinctEffectHinderedMind());
    public static final InstinctEffect EFFECT_HUNTED = instinctEffect("hunted", new InstinctEffectHunted());
    public static final InstinctEffect EFFECT_NETHER_FEVER = instinctEffect("nether_fever", new InstinctEffectNetherFever());
    public static final InstinctEffect EFFECT_TEMPERED_FLAME = instinctEffect("tempered_flame", new InstinctEffectTemperedFlame());
    public static final InstinctEffect EFFECT_OVERHEAT = instinctEffect("overheat", new InstinctEffectOverheat());

    // TODO: Nether instinct/effect (need will be the same)
    
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
    
    private static InstinctNeedFactory instinctNeed(String name, InstinctNeedFactory factory) {
        factory.setRegistryName(new ResourceLocation(HardcoreAlchemy.MOD_ID, name));
        INSTINCT_NEED_FACTORY_CACHE.add(factory);
        return factory;
    }
    
    private static InstinctNeedFactory instinctNeed(String name, IInstinctNeed instinctNeed) {
        InstinctNeedFactory factory = new InstinctNeedFactorySimple(instinctNeed);
        factory.setRegistryName(new ResourceLocation(HardcoreAlchemy.MOD_ID, name));
        INSTINCT_NEED_FACTORY_CACHE.add(factory);
        return factory;
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
