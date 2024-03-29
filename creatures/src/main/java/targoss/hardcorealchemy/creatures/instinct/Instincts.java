/*
 * Copyright 2017-2023 asanetargoss
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

package targoss.hardcorealchemy.creatures.instinct;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.IForgeRegistry;
import net.minecraftforge.fml.common.registry.RegistryBuilder;
import targoss.hardcorealchemy.HardcoreAlchemyCore;
import targoss.hardcorealchemy.coremod.HardcoreAlchemyPreInit;
import targoss.hardcorealchemy.creatures.instinct.api.Instinct;
import targoss.hardcorealchemy.creatures.instinct.api.InstinctEffect;
import targoss.hardcorealchemy.creatures.instinct.api.InstinctNeedFactory;
import targoss.hardcorealchemy.creatures.instinct.api.InstinctNeedFactorySimple;
import targoss.hardcorealchemy.registrar.Registrar;
import targoss.hardcorealchemy.registrar.RegistrarForge;

public class Instincts {
    public static final Registrar<Instinct> INSTINCTS = new RegistrarForge<Instinct>("instincts", HardcoreAlchemyCore.MOD_ID, HardcoreAlchemyPreInit.LOGGER);
    public static final Registrar<InstinctNeedFactory> INSTINCT_NEED_FACTORIES = new RegistrarForge<InstinctNeedFactory>("instinct need factories", HardcoreAlchemyCore.MOD_ID, HardcoreAlchemyPreInit.LOGGER);
    public static final Registrar<InstinctEffect> INSTINCT_EFFECTS = new RegistrarForge<InstinctEffect>("instinct needs", HardcoreAlchemyCore.MOD_ID, HardcoreAlchemyPreInit.LOGGER);

    public static final IForgeRegistry<Instinct> REGISTRY = new RegistryBuilder<Instinct>()
            .setName(new ResourceLocation(HardcoreAlchemyCore.MOD_ID, "instincts"))
            .setType(Instinct.class)
            .setIDRange(0, 1024)
            .create();
    
    public static final IForgeRegistry<InstinctNeedFactory> NEED_FACTORY_REGISTRY = new RegistryBuilder<InstinctNeedFactory>()
            .setName(new ResourceLocation(HardcoreAlchemyCore.MOD_ID, "instinct_factories"))
            .setType(InstinctNeedFactory.class)
            .setIDRange(0, 1024)
            .create();
    
    public static final IForgeRegistry<InstinctEffect> EFFECT_REGISTRY = new RegistryBuilder<InstinctEffect>()
            .setName(new ResourceLocation(HardcoreAlchemyCore.MOD_ID, "effects"))
            .setType(InstinctEffect.class)
            .setIDRange(0, 1024)
            .create();

    public static final Instinct PREDATOR = INSTINCTS.add("predator", new InstinctPredator());
    public static final Instinct HOMESICK_NATURE = INSTINCTS.add("homesick_nature", new InstinctHomesickNature());
    public static final Instinct HOMESICK_NETHER = INSTINCTS.add("homesick_nether", new InstinctHomesickNether());
    
    public static final InstinctNeedFactory NEED_ATTACK_PREY = INSTINCT_NEED_FACTORIES.add("attack_prey", new InstinctNeedFactorySimple(new InstinctNeedAttackPrey()));
    public static final InstinctNeedFactory NEED_SPAWN_ENVIRONMENT = INSTINCT_NEED_FACTORIES.add("environment", new IInstinctNeedEnvironment.Factory());
    
    public static final InstinctEffect EFFECT_HINDERED_MIND = INSTINCT_EFFECTS.add("hindered_mind", new InstinctEffectHinderedMind());
    public static final InstinctEffect EFFECT_HUNTED = INSTINCT_EFFECTS.add("hunted", new InstinctEffectHunted());
    public static final InstinctEffect EFFECT_NETHER_FEVER = INSTINCT_EFFECTS.add("nether_fever", new InstinctEffectNetherFever());
    public static final InstinctEffect EFFECT_TEMPERED_FLAME = INSTINCT_EFFECTS.add("tempered_flame", new InstinctEffectTemperedFlame());
    public static final InstinctEffect EFFECT_OVERHEAT = INSTINCT_EFFECTS.add("overheat", new InstinctEffectOverheat());
}
