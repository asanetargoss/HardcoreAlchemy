package targoss.hardcorealchemy.jei;

import java.util.ArrayList;
import java.util.List;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import targoss.hardcorealchemy.util.InventoryUtil;

public class RecipeWrapperIncantation implements IRecipeWrapper {
    public final RecipeIncantation recipe;

    public RecipeWrapperIncantation(RecipeIncantation recipe) {
        this.recipe = recipe;
    }

    @Override
    public void getIngredients(IIngredients paramIIngredients) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public List<ItemStack> getInputs() {
        List<ItemStack> inputs = new ArrayList<>();
        inputs.add(recipe.input);
        inputs.add(recipe.interact);
        return inputs;
    }

    @Override
    public List<ItemStack> getOutputs() {
        List<ItemStack> outputs = new ArrayList<>();
        outputs.add(recipe.output);
        return outputs;
    }

    @Override
    public List<FluidStack> getFluidInputs() {
        return null;
    }

    @Override
    public List<FluidStack> getFluidOutputs() {
        return null;
    }

    @Override
    public void drawInfo(Minecraft paramMinecraft, int backgroundWidth, int backgroundHeight, int mouseX, int mouseY) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void drawAnimations(Minecraft paramMinecraft, int backgroundWidth, int backgroundHeight) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY) {
        return null;
    }

    @Override
    public boolean handleClick(Minecraft paramMinecraft, int mouseX, int mouseY, int mouseButton) {
        return false;
    }

}
