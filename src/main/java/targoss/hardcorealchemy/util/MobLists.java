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
import net.minecraft.entity.EntityList;
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
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.entity.Entities;

/**
 * Various mob lists, mainly used for deciding which
 * mobs should have certain tweaks. Each received list
 * is a fresh copy. Each member in these lists
 * is the entity id string.
 *
 */
public class MobLists {
    
    public static Set<String> getBosses() {
        Set<String> bosses = new HashSet();
        
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
        
        return bosses;
    }

    public static Set<String> getNonMobs() {
        Set<String> nonMobs = new HashSet();
        
        // Hardcore Alchemy
        nonMobs.add(Entities.FISH_SWARM);
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
        
        return nonMobs;
    }

    public static Set<String> getHumans() {
        Set<String> humans = new HashSet();
        
        // Vanilla
        humans.add("Villager");
        humans.add("Witch");
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
        
        return humans;
    }

    public static Set<String> getPassiveMobs() {
        Set<String> passiveMobs = new HashSet();
        
        // Minecraft
        passiveMobs.add("Pig");
        passiveMobs.add("Cow");
        passiveMobs.add("Chicken");
        passiveMobs.add("Sheep");
        passiveMobs.add("EntityHorse");
        
        return passiveMobs;
    }

    /*
     * All classes of this set must be derived from EntityTameable
     */
    public static Set<String> getEntityTameables() {
        Set<String> entityTameables = new HashSet();
        
        // Minecraft
        entityTameables.add("Wolf");
        entityTameables.add("Ozelot");
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
        Set<String> nightMobs = new HashSet();
        
        // Minecraft
        nightMobs.add("Zombie");
        nightMobs.add("Skeleton");
        nightMobs.add("Creeper");
        nightMobs.add("Spider");
        nightMobs.add("Enderman");
        nightMobs.add("Silverfish");
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
        // Ars Magica Elementals
        nightMobs.add("arsmagica2.EarthElemental");
        nightMobs.add("arsmagica2.FireElemental");
        nightMobs.add("arsmagica2.ManaElemental");
        nightMobs.add("arsmagica2.WaterElemental");
        
        return nightMobs;
    }

    public static Set<String> getNetherMobs() {
        Set<String> netherMobs = new HashSet();
        
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
        
        return netherMobs;
    }
}
