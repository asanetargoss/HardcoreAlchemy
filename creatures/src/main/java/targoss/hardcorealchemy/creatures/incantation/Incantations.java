package targoss.hardcorealchemy.creatures.incantation;

import static targoss.hardcorealchemy.incantation.Incantations.INCANTATIONS;

import targoss.hardcorealchemy.incantation.api.Incantation;

public class Incantations {
    public static final Incantation CHANGE = INCANTATIONS.add("change", new IncantationChange());
}
