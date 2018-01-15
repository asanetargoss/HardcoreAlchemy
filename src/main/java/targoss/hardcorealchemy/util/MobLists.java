/*
 * Copyright 2017-2018 asanetargoss
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

package targoss.hardcorealchemy.util;

import java.util.HashSet;
import java.util.Set;

import mchorse.metamorph.entity.EntityMorph;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntitySilverfish;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.EntityWolf;

/**
 * Various mob lists, mainly used for deciding which
 * mobs should have certain tweaks. Each received list
 * is a fresh copy. Each member in these lists
 * is a string version of a class' fully-qualified name,
 * and represents that class specifically. All members
 * should be subclasses of {@link net.minecraft.entity.EntityLivingBase}.
 *
 */
public class MobLists {
    
    public static Set<String> getBosses() {
        Set<String> bosses = new HashSet();
        
        // Vanilla
        bosses.add(EntityDragon.class.getName());
        bosses.add(EntityWither.class.getName());
        // Ars Magica
        bosses.add("am2.bosses.EntityAirGuardian");
        bosses.add("am2.bosses.EntityArcaneGuardian");
        bosses.add("am2.bosses.EntityEarthGuardian");
        bosses.add("am2.bosses.EntityEnderGuardian");
        bosses.add("am2.bosses.EntityFireGuardian");
        bosses.add("am2.bosses.EntityLifeGuardian");
        bosses.add("am2.bosses.EntityLightningGuardian");
        bosses.add("am2.bosses.EntityNatureGuardian");
        bosses.add("am2.bosses.EntityWaterGuardian");
        bosses.add("am2.bosses.EntityWinterGuardian");
        // Voidcraft
        bosses.add("Tamaized.Voidcraft.entity.boss.dragon.sub.voidic.EntityVoidicDragon");
        bosses.add("Tamaized.Voidcraft.entity.boss.herobrine.extra.EntityHerobrineCreeper");
        bosses.add("Tamaized.Voidcraft.entity.boss.herobrine.EntityBossHerobrine");
        bosses.add("Tamaized.Voidcraft.entity.boss.lob.EntityLordOfBlades");
        bosses.add("Tamaized.Voidcraft.entity.boss.twins.EntityBossDol");
        bosses.add("Tamaized.Voidcraft.entity.boss.twins.EntityBossZol");
        // Wow, that's a lot of Xia bosses. I am not looking forward to that.
        bosses.add("Tamaized.Voidcraft.entity.boss.xia.EntityBossXia");
        bosses.add("Tamaized.Voidcraft.entity.boss.xia.EntityBossXia2");
        bosses.add("Tamaized.Voidcraft.entity.boss.xia.finalphase.EntityDragonXia");
        bosses.add("Tamaized.Voidcraft.entity.boss.xia.finalphase.EntityTwinsXia");
        bosses.add("Tamaized.Voidcraft.entity.boss.xia.finalphase.EntityWitherbrine");
        bosses.add("Tamaized.Voidcraft.entity.boss.xia.finalphase.EntityZolXia");
        bosses.add("Tamaized.Voidcraft.entity.boss.EntityBossCorruptedPawn");
        bosses.add("Tamaized.Voidcraft.entity.EntityVoidBoss");
        
        return bosses;
    }
    
    public static Set<String> getNonMobs() {
        Set<String> nonMobs = new HashSet();
        
        // Metamorph
        nonMobs.add("mchorse.metamorph.entity.EntityMorph");
        // Ars Magica
        nonMobs.add("am2.entity.EntityAirSled");
        nonMobs.add("am2.entity.EntityBroom");
        nonMobs.add("am2.entity.EntityDummyCaster");
        nonMobs.add("am2.entity.EntityRiftStorage");
        nonMobs.add("am2.entity.EntityShadowHelper");
        nonMobs.add("am2.entity.EntityThrownRock");
        nonMobs.add("am2.entity.EntityThrownSickle");
        nonMobs.add("am2.entity.EntityWinterGuardianArm");
        
        return nonMobs;
    }
    
    public static Set<String> getHumans() {
        Set<String> humans = new HashSet();
        
        // Vanilla
        humans.add(EntityVillager.class.getName());
        humans.add(EntityWitch.class.getName());
        // Ars Magica
        humans.add("am2.entity.EntityDryad");
        humans.add("am2.entity.EntityLightMage");
        humans.add("am2.entity.EntityDarkMage");
        // Minecraft Comes Alive
        humans.add("mca.entity.EntityHuman");
        // ToroQuest
        humans.add("net.torocraft.toroquest.entities.EntityFugitive");
        humans.add("net.torocraft.toroquest.entities.EntityGuard");
        humans.add("net.torocraft.toroquest.entities.EntityMage");
        humans.add("net.torocraft.toroquest.entities.EntityFugitive");
        humans.add("net.torocraft.toroquest.entities.EntityRainbowGuard");
        humans.add("net.torocraft.toroquest.entities.EntityRainbowKing");
        humans.add("net.torocraft.toroquest.entities.EntitySentry");
        humans.add("net.torocraft.toroquest.entities.EntityShopkeeper");
        humans.add("net.torocraft.toroquest.entities.EntityFugitive");
        humans.add("net.torocraft.toroquest.entities.EntityVillageLord");
        
        return humans;
    }
    
    public static Set<String> getPassiveMobs() {
        Set<String> passiveMobs = new HashSet();
        
        // Minecraft
        passiveMobs.add(EntityPig.class.getName());
        passiveMobs.add(EntityCow.class.getName());
        passiveMobs.add(EntityChicken.class.getName());
        passiveMobs.add(EntitySheep.class.getName());
        passiveMobs.add(EntityHorse.class.getName());
        
        return passiveMobs;
    }
    
    /*
     * All classes of this set must be derived from EntityTameable
     */
    public static Set<String> getEntityTameables() {
        Set<String> entityTameables = new HashSet();
        
        // Minecraft
        entityTameables.add(EntityWolf.class.getName());
        entityTameables.add(EntityOcelot.class.getName());
        // Voidcraft
        entityTameables.add("Tamaized.Voidcraft.entity.companion.EntityCompanion");
        entityTameables.add("Tamaized.Voidcraft.entity.companion.EntityCompanionFireElemental");
        // ToroQuest
        entityTameables.add("net.torocraft.toroquest.entities.EntityToro");
        
        return entityTameables;
    }
    
    /**
     * Overworld mobs which spawn in darkness
     */
    public static Set<String> getNightMobs() {
        Set<String> nightMobs = new HashSet();
        
        // Minecraft
        nightMobs.add(EntityZombie.class.getName());
        nightMobs.add(EntitySkeleton.class.getName());
        nightMobs.add(EntityCreeper.class.getName());
        nightMobs.add(EntitySpider.class.getName());
        nightMobs.add(EntityEnderman.class.getName());
        nightMobs.add(EntitySilverfish.class.getName());
        nightMobs.add(EntitySlime.class.getName());
        nightMobs.add(EntityBat.class.getName());
        // Deadly Monsters
        nightMobs.add("com.dmonsters.entity.EntityBaby");
        nightMobs.add("com.dmonsters.entity.EntityClimber");
        nightMobs.add("com.dmonsters.entity.EntityEntrail");
        nightMobs.add("com.dmonsters.entity.EntityFreezer");
        nightMobs.add("com.dmonsters.entity.EntityMutantSteve");
        nightMobs.add("com.dmonsters.entity.EntityPresent");
        nightMobs.add("com.dmonsters.entity.EntityWideman");
        nightMobs.add("com.dmonsters.entity.EntityWoman");
        nightMobs.add("com.dmonsters.entity.EntityZombieChicken");
        //TODO: Ars Magica Elementals
        
        return nightMobs;
    }
    
    public static Set<String> getNetherMobs() {
        Set<String> netherMobs = new HashSet();
        
        // Minecraft
        netherMobs.add(EntityPigZombie.class.getName());
        netherMobs.add(EntityGhast.class.getName());
        netherMobs.add(EntityBlaze.class.getName());
        netherMobs.add(EntityMagmaCube.class.getName());
        // Ad Inferos
        netherMobs.add("com.superdextor.adinferos.entity.monster.EntityBlackWidow");
        netherMobs.add("com.superdextor.adinferos.entity.monster.EntityCurse");
        netherMobs.add("com.superdextor.adinferos.entity.monster.EntityGhost");
        netherMobs.add("com.superdextor.adinferos.entity.monster.EntityGlowstoneSkeleton");
        netherMobs.add("com.superdextor.adinferos.entity.monster.EntityHerobrine");
        netherMobs.add("com.superdextor.adinferos.entity.monster.EntityHerobrineClone");
        netherMobs.add("com.superdextor.adinferos.entity.monster.EntityInfernumAvis");
        netherMobs.add("com.superdextor.adinferos.entity.monster.EntityObsidianSheepman");
        netherMobs.add("com.superdextor.adinferos.entity.monster.EntityPhantom");
        netherMobs.add("com.superdextor.adinferos.entity.monster.EntityReaper");
        netherMobs.add("com.superdextor.adinferos.entity.monster.EntitySkeletonHorse");
        netherMobs.add("com.superdextor.adinferos.entity.monster.EntitySummoner");
        
        return netherMobs;
    }
}
