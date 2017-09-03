package targoss.hardcorealchemy.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mchorse.metamorph.api.morphs.AbstractMorph;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityEndermite;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.entity.monster.EntityPolarBear;
import net.minecraft.entity.monster.EntityShulker;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntitySquid;

public class MorphDiet {
    
    /**
     * Dietary needs indexed by the fully-qualified name of the entity class which the morph represents
     */
    private static Map<String, Needs> morphDiets = new HashMap<String, Needs>();
    public static final Needs PLAYER_NEEDS = new Needs();
    public static final Needs IDEALIST_VEGAN_NEEDS = new Needs(Needs.DEFAULT_NUTRIENTS, Restriction.VEGAN);
    public static final Needs GRAZER_NEEDS = new Needs(new String[]{"grain"}, Restriction.VEGAN);
    public static final Needs NIGHT_MOB_NEEDS = new Needs(Needs.CARNIVORE_NUTRIENTS, Restriction.CARNIVORE);
    public static final Needs NETHER_MOB_NEEDS = new Needs(Needs.CARNIVORE_NUTRIENTS, Restriction.CARNIVORE, false);
    
    static {
        /* With some exceptions, hostile mobs and tameables are carnivores,
         * and Nether mobs are also thirstless.
         * Passive mobs are vegans and eat wheat.
         * Exceptions/uncategorized mobs are at the top, and will not be overridden.
         */
        
        morphDiets.put(entityName(EntityCreeper.class), IDEALIST_VEGAN_NEEDS);
        morphDiets.put(entityName(EntitySlime.class), PLAYER_NEEDS);
        morphDiets.put(entityName(EntityChicken.class), IDEALIST_VEGAN_NEEDS);
        morphDiets.put(entityName(EntityPig.class), IDEALIST_VEGAN_NEEDS);
        morphDiets.put("net.torocraft.toroquest.entities.EntityToro", GRAZER_NEEDS);
        morphDiets.put(entityName(EntityEnderman.class), new Needs(Needs.DEFAULT_NUTRIENTS, Restriction.OMNIVORE, false));
        morphDiets.put(entityName(EntityEndermite.class), new Needs(Needs.NO_NUTRIENTS, Restriction.OMNIVORE, false));
        morphDiets.put(entityName(EntitySquid.class), new Needs(Needs.CARNIVORE_NUTRIENTS, Restriction.OMNIVORE, false));
        morphDiets.put(entityName(EntityGuardian.class), new Needs(Needs.CARNIVORE_NUTRIENTS, Restriction.OMNIVORE, false));
        morphDiets.put(entityName(EntityShulker.class), new Needs(Needs.NO_NUTRIENTS, Restriction.UNFEEDING, false));
        morphDiets.put(entityName(EntityPolarBear.class), new Needs(Needs.CARNIVORE_NUTRIENTS, Restriction.OMNIVORE));
        
        for (String nightMob : MobLists.getNightMobs()) {
            if (!morphDiets.containsKey(nightMob)) {
                morphDiets.put(nightMob, NIGHT_MOB_NEEDS);
            }
        }
        for (String tameable : MobLists.getEntityTameables()) {
            if (!morphDiets.containsKey(tameable)) {
                morphDiets.put(tameable, NIGHT_MOB_NEEDS);
            }
        }
        for (String netherMob : MobLists.getNetherMobs()) {
            if (!morphDiets.containsKey(netherMob)) {
                morphDiets.put(netherMob, NETHER_MOB_NEEDS);
            }
        }
        for (String passiveMob : MobLists.getPassiveMobs()) {
            if (!morphDiets.containsKey(passiveMob)) {
                morphDiets.put(passiveMob, GRAZER_NEEDS);
            }
        }
    }
    
    public static String entityName(Class<? extends Entity> clazz) {
        return EntityList.CLASS_TO_NAME.get(clazz.getName());
    }
    
    public static String entityName(String className) {
        return EntityList.CLASS_TO_NAME.get(className);
    }
    
    /**
     * Get which nutritional needs and restrictions are enabled for this morph
     */
    public static Needs getNeeds(AbstractMorph morph) {
        if (morph == null) {
            return PLAYER_NEEDS;
        }
        Needs needs = morphDiets.get(EntityList.NAME_TO_CLASS.get(morph.name).getName());
        if (needs == null) {
            return PLAYER_NEEDS;
        }
        return needs;
    }
    
    public static enum Restriction {
        OMNIVORE(""),
        CARNIVORE("The idea of eating pacifist weakling food is disgusting to you."),
        VEGAN("You cannot stand the thought of exploiting another living being for food."),
        // Does not need to eat, and cannot eat
        UNFEEDING("You perceive no practical use in this chunk of matter.")
        ;
        
        public final String cantEatReason;
        
        private Restriction(String cantEatReason) {
            this.cantEatReason = cantEatReason;
        }
    }
    
    public static class Needs {
        public static final String[] DEFAULT_NUTRIENTS = new String[]{"dairy","fruit","grain","protein","vegetable"};
        public static final String[] CARNIVORE_NUTRIENTS = new String[]{"dairy","protein"};
        public static final String[] NO_NUTRIENTS = new String[]{};
        
        public final String[] nutrients;
        protected final Set<String> nutrientSet;
        public final Restriction restriction;
        public final boolean hasThirst;
        
        public Needs() {
            this(DEFAULT_NUTRIENTS, Restriction.OMNIVORE, true);
        }
        
        public Needs(String[] nutrients, Restriction restriction) {
            this(nutrients, restriction, true);
        }
        
        public Needs(String[] nutrients, Restriction restriction, boolean thirst) {
            this.nutrients = (restriction != Restriction.UNFEEDING) ? nutrients : NO_NUTRIENTS;
            this.hasThirst = thirst;
            this.restriction = restriction;
            
            Set<String> nutrientSet = new HashSet<String>();
            for (String nutrient : this.nutrients) {
                nutrientSet.add(nutrient);
            }
            this.nutrientSet = nutrientSet;
        }
        
        public boolean containsNutrient(String nutrient) {
            return nutrientSet.contains(nutrient);
        }
    }
}
