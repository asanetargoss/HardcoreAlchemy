package targoss.hardcorealchemy.jei;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import targoss.hardcorealchemy.incantation.api.Incantation;

public class RecipeIncantation {
    public final Incantation incantation;
    public final ItemStack input;
    public final ItemStack output;
    public final ItemStack interact;
    
    public RecipeIncantation(Incantation incantation, @Nullable ItemStack input, @Nullable ItemStack output, @Nullable ItemStack interact) {
        this.incantation = incantation;
        this.input = input;
        this.output = output;
        this.interact = interact;
    }
}
