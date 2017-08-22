package targoss.hardcorealchemy.util;

import java.util.HashSet;
import java.util.Set;

import mchorse.metamorph.entity.EntityMorph;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.passive.EntityVillager;

/**
 * Various mob lists, mainly used for deciding which
 * mobs should have certain tweaks. Changing these
 * lists after Minecraft has loaded should not affect
 * gameplay and instead be copied elsewhere for
 * more specific purposes. Each member in these lists
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
        //TODO: add the rest of the bosses from Ars Magica, Voidcraft, etc...
        
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
}
