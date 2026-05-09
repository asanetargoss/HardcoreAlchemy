package targoss.hardcorealchemy.tweaks.incantation;

import static targoss.hardcorealchemy.incantation.Incantations.INCANTATIONS;

import targoss.hardcorealchemy.incantation.api.Incantation;

public class Incantations {
    public static final Incantation INCANTATION_FLINT_AND_STEEL = INCANTATIONS.add("flint_and_steel", new IncantationFlintAndSteel());
}
