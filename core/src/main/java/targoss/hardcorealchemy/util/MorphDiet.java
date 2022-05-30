/*
 * Copyright 2017-2022 asanetargoss
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

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

public class MorphDiet {
    public static final Needs PLAYER_NEEDS = new Needs();
    
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
        public final boolean hasHunger;
        
        public Needs() {
            this(DEFAULT_NUTRIENTS, Restriction.OMNIVORE, true);
        }
        
        public Needs(String[] nutrients, Restriction restriction) {
            this(nutrients, restriction, true);
        }
        
        public Needs(String[] nutrients, Restriction restriction, boolean thirst) {
            this.nutrients = !restriction.equals(Restriction.UNFEEDING) ? nutrients : NO_NUTRIENTS;
            this.hasThirst = thirst;
            this.hasHunger = !restriction.equals(Restriction.UNFEEDING);
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
