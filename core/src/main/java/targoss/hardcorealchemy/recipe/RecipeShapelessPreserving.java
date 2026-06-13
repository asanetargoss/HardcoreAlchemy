package targoss.hardcorealchemy.recipe;

import static targoss.hardcorealchemy.util.InventoryUtil.isEmptyItemStack;

import java.util.List;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapelessRecipes;

public class RecipeShapelessPreserving extends ShapelessRecipes {
    protected List<Boolean> preservedInputList;
    public RecipeShapelessPreserving(ItemStack output, List<ItemStack> inputList, List<Boolean> preservedInputList) {
        super(output, inputList);
        assert(inputList.size() == preservedInputList.size());
        this.preservedInputList = preservedInputList;
    }

    @Override
    public ItemStack[] getRemainingItems(InventoryCrafting inv)
    {
        ItemStack[] remainingItems = new ItemStack[inv.getSizeInventory()];

        for (int i = 0; i < remainingItems.length; ++i)
        {
            ItemStack itemStack = inv.getStackInSlot(i);
            if (isEmptyItemStack(itemStack)) {
                continue;
            }
            boolean preserved = false;
            for (int j = 0; j < recipeItems.size(); ++j) {
                ItemStack input = recipeItems.get(j);
                boolean preservedInput = preservedInputList.get(j);
                if (!preservedInput) {
                    continue;
                }
                if (itemStack.getItem() != input.getItem() || (input.getMetadata() != 32767 && itemStack.getMetadata() != input.getMetadata())) {
                    continue;
                }
                preserved = true;
                break;
            }
            if (preserved) {
                // HACK: Increment the stack size to counteract the decrease in stack size later during crafting
                ++itemStack.stackSize;
            } else {
                remainingItems[i] = net.minecraftforge.common.ForgeHooks.getContainerItem(itemStack);
            }
        }

        return remainingItems;
    }
}
