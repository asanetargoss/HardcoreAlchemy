package targoss.hardcorealchemy.util;

import java.util.EnumSet;
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
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

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
        return EntityList.CLASS_TO_NAME.get(clazz);
    }
    
    public static String entityName(String className) {
        try {
            return EntityList.CLASS_TO_NAME.get(Class.forName(className));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Get which nutritional needs and restrictions are enabled for this morph
     */
    public static Needs getNeeds(AbstractMorph morph) {
        if (morph == null) {
            return PLAYER_NEEDS;
        }
        Needs needs = morphDiets.get(morph.name);
        if (needs == null) {
            return PLAYER_NEEDS;
        }
        return needs;
    }
    
    /** Do not change the name of these enums as they are used in serialization
     */
    public static enum Restriction {
        OMNIVORE("",
                "hardcorealchemy.diet.display.omnivore.upper", TextFormatting.GOLD),
        CARNIVORE("hardcorealchemy.diet.refuse.carnivore",
                "hardcorealchemy.diet.display.carnivore.upper", TextFormatting.RED),
        VEGAN("hardcorealchemy.diet.refuse.vegan",
                "hardcorealchemy.diet.display.vegan.upper", TextFormatting.GREEN),
        // Does not need to eat, and cannot eat
        UNFEEDING("hardcorealchemy.diet.refuse.unfeeding",
                "hardcorealchemy.diet.display.unfeeding.upper", TextFormatting.DARK_GRAY)
        ;
        
        private final String cantEatReason;
        private final String displayName;
        private final Style tooltipStyle;
        
        private Restriction(String cantEatReason, String displayName, TextFormatting tooltipColor) {
            this.cantEatReason = cantEatReason;
            this.displayName = displayName;
            this.tooltipStyle = new Style().setColor(tooltipColor);
        }
        
        public ITextComponent getFoodTooltip() {
            return new TextComponentTranslation(displayName).setStyle(tooltipStyle);
        }
        
        public ITextComponent getFoodRefusal() {
            return new TextComponentTranslation(cantEatReason);
        }
        
        private static Map<String, Restriction> stringMap;
        
        static {
            stringMap = new HashMap<String, Restriction>();
            for (Restriction restriction : EnumSet.allOf(Restriction.class)) {
                stringMap.put(restriction.toString(), restriction);
            }
        }
        
        public static Restriction fromString(String str) {
            return stringMap.get(str);
        }
        
        public static Restriction merge(Restriction left, Restriction right) {
            if (left == null) {
                return right;
            }
            if (left == right || right == null) {
                return left;
            }
            
            switch (left) {
            case OMNIVORE:
                return OMNIVORE;
            case CARNIVORE:
                if (right == VEGAN || right == OMNIVORE) {
                    return OMNIVORE;
                }
                return CARNIVORE;
            case VEGAN:
                if (right == CARNIVORE || right == OMNIVORE) {
                    return OMNIVORE;
                }
                return VEGAN;
            case UNFEEDING:
                return right;
            default:
                return left;
            }
        }
        
        public boolean canEat(Restriction foodRestriction) {
            if (foodRestriction == UNFEEDING) {
                return true;
            }
            else if (this == UNFEEDING) {
                return false;
            }
            if (foodRestriction == null || this == OMNIVORE) {
                return true;
            }
            return this == foodRestriction;
        }
    }
    
    public static class Needs {
        public static final String[] DEFAULT_NUTRIENTS = new String[]{"dairy","fruit","grain","protein","vegetable"};
        public static final String[] CARNIVORE_NUTRIENTS = new String[]{"protein"};
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
        
        public float getNutrientDecay(String nutrient) {
            float nutrientCount = (float)(nutrientSet.size());
            if (nutrientCount == 0 || !nutrientSet.contains(nutrient)) {
                throw new IllegalArgumentException("Attempted to get decay rate for nutrient not part of needs");
            }
            return DEFAULT_NUTRIENTS.length / nutrientCount;
        }
    }
}
