/*
 * Copyright 2017-2023 asanetargoss
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

package targoss.hardcorealchemy.util;

import java.util.HashSet;
import java.util.Set;

/**
 * Various mob lists, mainly used for deciding which
 * mobs should have certain tweaks. Each received list
 * is a fresh copy. Each member in these lists
 * is the entity id string.
 *
 */
public class MobLists {

    public static final String PIG = "Pig";
    public static final String WOLF = "Wolf";
    public static final String OCELOT = "Ozelot";
    public static final String POLAR_BEAR = "PolarBear";
    public static final String ZOMBIE = "Zombie";
    public static final String SKELETON = "Skeleton";
    public static final String SPIDER = "Spider";
    public static final String CREEPER = "Creeper";
    public static final String CAVE_SPIDER = "CaveSpider";
    public static final String SILVERFISH = "Silverfish";
    public static final String ENDERMAN = "Enderman";
    
    public static Set<String> getBosses() {
        Set<String> bosses = new HashSet<>();
        
        // Vanilla
        bosses.add("EnderDragon");
        bosses.add("WitherBoss");
        // Ars Magica
        bosses.add("arsmagica2.AirGuardian");
        bosses.add("arsmagica2.ArcaneGuardian");
        bosses.add("arsmagica2.EarthGuardian");
        bosses.add("arsmagica2.EnderGuardian");
        bosses.add("arsmagica2.FireGuardian");
        bosses.add("arsmagica2.LifeGuardian");
        bosses.add("arsmagica2.LightningGuardian");
        bosses.add("arsmagica2.NatureGuardian");
        bosses.add("arsmagica2.WaterGuardian");
        bosses.add("arsmagica2.WinterGuardian");
        // Voidcraft
        bosses.add("voidcraft.VoidicDragon");
        bosses.add("voidcraft.HerobrineCreeper");
        bosses.add("voidcraft.BossHerobrine");
        bosses.add("voidcraft.LordOfBlades");
        bosses.add("voidcraft.Dol");
        bosses.add("voidcraft.Zol");
        // Wow, that's a lot of Xia bosses. I am not looking forward to that.
        bosses.add("voidcraft.Xia");
        bosses.add("voidcraft.Xia2");
        bosses.add("voidcraft.DragonXia");
        bosses.add("voidcraft.Witherbrine");
        bosses.add("voidcraft.ZolXia");
        bosses.add("voidcraft.VoidBoss");
        // Thaumcraft
        // (pretty sure these are not implemented yet...)
        bosses.add("thaumcraft.EldritchWarden");
        bosses.add("thaumcraft.EldritchGolem");
        bosses.add("thaumcraft.CultistLeader");
        bosses.add("thaumcraft.TaintacleGiant");
        
        return bosses;
    }

    public static Set<String> getNonMobs() {
        Set<String> nonMobs = new HashSet<>();
        
        // Hardcore Alchemy
        nonMobs.add("hardcorealchemy.fish_swarm");
        // Metamorph
        nonMobs.add("metamorph.Morph");
        // Ars Magica
        nonMobs.add("arsmagica2.AirSled");
        nonMobs.add("arsmagica2.Broom");
        nonMobs.add("arsmagica2.RiftStorage");
        nonMobs.add("arsmagica2.ShadowHelper");
        nonMobs.add("arsmagica2.ThrownRock");
        nonMobs.add("arsmagica2.ThrownSickle");
        nonMobs.add("arsmagica2.WinterGuardianArm");
        // Thaumcraft
        nonMobs.add("thaumcraft.TaintSwarm");
        nonMobs.add("thaumcraft.CultistPortalGreater");
        nonMobs.add("thaumcraft.CultistPortalLesser");
        nonMobs.add("thaumcraft.SpecialItem");
        nonMobs.add("thaumcraft.FollowItem");
        nonMobs.add("thaumcraft.FallingTaint");
        nonMobs.add("thaumcraft.Alumentum");
        nonMobs.add("thaumcraft.GolemDart");
        nonMobs.add("thaumcraft.EldritchOrb");
        nonMobs.add("thaumcraft.BottleTaint");
        nonMobs.add("thaumcraft.GolemOrb");
        nonMobs.add("thaumcraft.Grapple");
        nonMobs.add("thaumcraft.FocusProjectile");
        nonMobs.add("thaumcraft.FocusCloud");
        nonMobs.add("thaumcraft.Focusmine");
        nonMobs.add("thaumcraft.TurretBasic");
        nonMobs.add("thaumcraft.TurretAdvanced");
        nonMobs.add("thaumcraft.ArcaneBore");
        nonMobs.add("thaumcraft.Golem");
        nonMobs.add("thaumcraft.Spellbat");
        // Jon's Useless Mod
        nonMobs.add("jum.Useless Arrow");
        
        return nonMobs;
    }

    public static Set<String> getHumans() {
        Set<String> humans = new HashSet<>();
        
        // Vanilla
        humans.add("Villager");
        // Ars Magica
        humans.add("arsmagica2.Dryad");
        humans.add("arsmagica2.LightMage");
        humans.add("arsmagica2.DarkMage");
        // Minecraft Comes Alive
        humans.add("MCA.EntityHuman");
        // ToroQuest
        humans.add("toroquest.fugitive");
        humans.add("toroquest.guard");
        humans.add("toroquest.mage");
        humans.add("toroquest.rainbow_guard");
        humans.add("toroquest.rainbow_king");
        humans.add("toroquest.sentry");
        humans.add("toroquest.shopkeeper");
        humans.add("toroquest.village_lord");
        // Village Box
        humans.add("villagebox.villager");
        // Thaumcraft
        // Yes I realize this is technically not human, but close enough.
        humans.add("thaumcraft.Pech");
        // Meme in a Bottle
        humans.add("miab.JohnCena");
        humans.add("miab.BillCipher");
        humans.add("miab.Donaldtrump");
        humans.add("miab.EduardKhil");
        humans.add("miab.MoonMan");
        humans.add("miab.RottenRobbie");
        humans.add("miab.Nigelthornberry");
        humans.add("miab.PPAPGuy");
        humans.add("miab.GrandDad");
        humans.add("miab.ForeverAlone");
        
        return humans;
    }
    
    public static Set<String> getHumanoids() {
        Set<String> humanoids = getHumans();
        
        // Vanilla
        humanoids.add(ZOMBIE);
        humanoids.add(SKELETON);
        humanoids.add("PigZombie");
        humanoids.add(ENDERMAN);
        // Ender Zoo
        humanoids.add("EnderZoo.Enderminy");
        humanoids.add("EnderZoo.FallenKnight");
        // Thaumcraft
        humanoids.add("thaumcraft.EldritchGuardian");
        humanoids.add("thaumcraft.BrainyZombie");
        humanoids.add("thaumcraft.GiantBrainyZombie");
        humanoids.add("thaumcraft.InhabitedZombie");
        humanoids.add("thaumcraft.CultistKnight");
        humanoids.add("thaumcraft.CultistCleric");
        humanoids.add("thaumcraft.CultistLeader");
        // Deadly Monsters
        humanoids.add("dmonsters.baby");
        humanoids.add("dmonsters.climber");
        humanoids.add("dmonsters.mutantSteve");
        humanoids.add("dmonsters.wideman");
        humanoids.add("dmonsters.woman");
        // Ad Inferos
        humanoids.add("adinferos.Ghost");
        humanoids.add("adinferos.GlowstoneSkeleton");
        humanoids.add("adinferos.Herobrine");
        humanoids.add("adinferos.HerobrineClone");
        humanoids.add("adinferos.ObsidianSheepman");
        humanoids.add("adinferos.Phantom");
        humanoids.add("adinferos.Reaper");
        humanoids.add("adinferos.Summoner");
        // Meme in a Bottle
        humanoids.add("miab.Shrek");
        humanoids.add("miab.DatBoi");
        humanoids.add("miab.Sanic");
        humanoids.add("miab.Pepe");
        humanoids.add("miab.DankeyKang");
        
        return humanoids;
    }

    public static Set<String> getPassiveMobs() {
        Set<String> passiveMobs = new HashSet<>();
        
        // Minecraft
        passiveMobs.add(PIG);
        passiveMobs.add("Cow");
        passiveMobs.add("Chicken");
        passiveMobs.add("Sheep");
        passiveMobs.add("EntityHorse");
        passiveMobs.add("Rabbit");
        // Ender Zoo
        passiveMobs.add("EnderZoo.Owl");
        
        return passiveMobs;
    }
    
    /**
     * Mobs that like spawning on grass during the day
     */
    public static Set<String> getGrassMobs() {
        Set<String> grassMobs = new HashSet<>();
        
        // Minecraft
        grassMobs.add(PIG);
        grassMobs.add("Cow");
        grassMobs.add("Chicken");
        grassMobs.add("Sheep");
        grassMobs.add("EntityHorse");
        grassMobs.add(WOLF);
        grassMobs.add(OCELOT);
        grassMobs.add("Rabbit");
        // ToroQuest
        grassMobs.add("toroquest.toro");
        
        return grassMobs;
    }
    
    /**
     * Mobs that are considered natural animals and spawn in a certain location on land
     */
    public static Set<String> getLandAnimals() {
        Set<String> landAnimals = new HashSet<>();
        
        // Minecraft
        landAnimals.add(PIG);
        landAnimals.add("Cow");
        landAnimals.add("Chicken");
        landAnimals.add("Sheep");
        landAnimals.add("EntityHorse");
        landAnimals.add(WOLF);
        landAnimals.add(OCELOT);
        landAnimals.add("Rabbit");
        landAnimals.add(POLAR_BEAR);
        landAnimals.add("MushroomCow");
        // ToroQuest
        landAnimals.add("toroquest.toro");
        // Ender Zoo
        landAnimals.add("EnderZoo.DireWolf");
        // Sophisticated Wolves
        landAnimals.add("SophisticatedWolves.SWWolf");
        
        return landAnimals;
    }

    /*
     * All classes of this set must be derived from EntityTameable
     */
    public static Set<String> getEntityTameables() {
        Set<String> entityTameables = new HashSet<>();
        
        // Minecraft
        entityTameables.add(WOLF);
        entityTameables.add(OCELOT);
        // Voidcraft
        entityTameables.add("voidcraft.FireElemental");
        // ToroQuest
        entityTameables.add("toroquest.toro");
        
        return entityTameables;
    }

    /**
     * Overworld mobs which spawn in darkness
     */
    public static Set<String> getNightMobs() {
        Set<String> nightMobs = new HashSet<>();
        
        // Minecraft
        nightMobs.add(ZOMBIE);
        nightMobs.add(SKELETON);
        nightMobs.add(CREEPER);
        nightMobs.add(SPIDER);
        nightMobs.add(CAVE_SPIDER);
        nightMobs.add(ENDERMAN);
        nightMobs.add(SILVERFISH);
        nightMobs.add("Slime");
        nightMobs.add("Bat");
        // Deadly Monsters
        nightMobs.add("dmonsters.baby");
        nightMobs.add("dmonsters.climber");
        nightMobs.add("dmonsters.entrail");
        nightMobs.add("dmonsters.freezer");
        nightMobs.add("dmonsters.mutantSteve");
        nightMobs.add("dmonsters.present");
        nightMobs.add("dmonsters.wideman");
        nightMobs.add("dmonsters.woman");
        nightMobs.add("dmonsters.zombieChicken");
        // Thaumcraft
        nightMobs.add("thaumcraft.BrainyZombie");
        nightMobs.add("thaumcraft.GiantBrainyZombie");
        nightMobs.add("thaumcraft.InhabitedZombie");
        // Ender Zoo
        nightMobs.add("EnderZoo.Enderminy");
        nightMobs.add("EnderZoo.ConcussionCreeper");
        nightMobs.add("EnderZoo.FallenKnight");
        nightMobs.add("EnderZoo.FallenMount");
        nightMobs.add("EnderZoo.WitherCat");
        
        return nightMobs;
    }

    public static Set<String> getNetherMobs() {
        Set<String> netherMobs = new HashSet<>();
        
        // Minecraft
        netherMobs.add("PigZombie");
        netherMobs.add("Ghast");
        netherMobs.add("Blaze");
        netherMobs.add("LavaSlime");
        // Ad Inferos
        netherMobs.add("adinferos.BlackWidow");
        netherMobs.add("adinferos.Curse");
        netherMobs.add("adinferos.Ghost");
        netherMobs.add("adinferos.GlowstoneSkeleton");
        netherMobs.add("adinferos.Herobrine");
        netherMobs.add("adinferos.HerobrineClone");
        netherMobs.add("adinferos.InfernalChicken");
        netherMobs.add("adinferos.InfernumAvis");
        netherMobs.add("adinferos.ObsidianSheepman");
        netherMobs.add("adinferos.Phantom");
        netherMobs.add("adinferos.Reaper");
        netherMobs.add("adinferos.SkeletonHorse");
        netherMobs.add("adinferos.Summoner");
        // Thaumcraft
        netherMobs.add("thaumcraft.Firebat");
        
        return netherMobs;
    }

    public static Set<String> getAuraMobs() {
        Set<String> auraMobs = new HashSet<>();
        
        // Thaumcraft
        auraMobs.add("thaumcraft.Wisp");
        // Ars Magica Elementals
        auraMobs.add("arsmagica2.EarthElemental");
        auraMobs.add("arsmagica2.FireElemental");
        auraMobs.add("arsmagica2.ManaElemental");
        auraMobs.add("arsmagica2.WaterElemental");
        
        return auraMobs;
    }

    public static Set<String> getTaintMobs() {
        Set<String> taintMobs = new HashSet<>();

        taintMobs.add("thaumcraft.ThaumSlime");
        taintMobs.add("thaumcraft.TaintCrawler");
        taintMobs.add("thaumcraft.Taintacle");
        taintMobs.add("thaumcraft.TaintacleTiny");
        taintMobs.add("thaumcraft.TaintSwarm");
        taintMobs.add("thaumcraft.TaintSeed");
        taintMobs.add("thaumcraft.TaintSeedPrime");
        
        return taintMobs;
    }

    public static Set<String> getEldritchMobs() {
        Set<String> eldritchMobs = new HashSet<>();

        eldritchMobs.add("thaumcraft.MindSpider");
        eldritchMobs.add("thaumcraft.EldritchGuardian");
        eldritchMobs.add("thaumcraft.EldritchCrab");
        
        return eldritchMobs;
    }

    public static Set<String> getTrollMobs() {
        Set<String> trollMobs = new HashSet<>();

        // Jon's Useless Mod
        trollMobs.add("jum.Dave the Useless");
        // Meme in a Bottle
        trollMobs.add("miab.memeSplash");
        trollMobs.add("miab.DogeWolf");
        trollMobs.add("miab.GrumpyCat");
        trollMobs.add("miab.PPAPGuy");
        trollMobs.add("miab.Shrek");
        trollMobs.add("miab.DatBoi");
        trollMobs.add("miab.GrandDad");
        trollMobs.add("miab.Sanic");
        trollMobs.add("miab.Pepe");
        trollMobs.add("miab.ForeverAlone");
        trollMobs.add("miab.NyanCat");
        trollMobs.add("miab.TacNyan");
        trollMobs.add("miab.DankeyKang");
        trollMobs.add("miab.Donaldtrump");
        trollMobs.add("miab.EduardKhil");
        trollMobs.add("miab.MoonMan");
        trollMobs.add("miab.RottenRobbie");
        trollMobs.add("miab.Nigelthornberry");
        
        return trollMobs;
    }

    /**
     * Morphs of these mobs do not increase max humanity
     * when acquired.
     */
    public static Set<String> getFreebieMobs() {
        Set<String> freebieMobs = new HashSet<>();

        // Meme in a Bottle
        freebieMobs.add("miab.memeSplash");
        freebieMobs.add("miab.DogeWolf");
        freebieMobs.add("miab.GrumpyCat");
        freebieMobs.add("miab.PPAPGuy");
        freebieMobs.add("miab.Shrek");
        freebieMobs.add("miab.DatBoi");
        freebieMobs.add("miab.GrandDad");
        freebieMobs.add("miab.Sanic");
        freebieMobs.add("miab.Pepe");
        freebieMobs.add("miab.ForeverAlone");
        freebieMobs.add("miab.NyanCat");
        freebieMobs.add("miab.TacNyan");
        freebieMobs.add("miab.DankeyKang");
        freebieMobs.add("miab.Donaldtrump");
        freebieMobs.add("miab.EduardKhil");
        freebieMobs.add("miab.MoonMan");
        freebieMobs.add("miab.RottenRobbie");
        freebieMobs.add("miab.Nigelthornberry");
        
        return freebieMobs;
    }

}
