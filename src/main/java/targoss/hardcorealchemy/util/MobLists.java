package targoss.hardcorealchemy.util;

import java.util.HashSet;
import java.util.Set;

import mchorse.metamorph.entity.EntityMorph;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.passive.EntityVillager;

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
        //TODO: Add the rest of human-like mobs from MCA and possibly ToroQuest if we keep that one
        
        return humans;
    }
    
    //TODO: tameables
}
