package targoss.hardcorealchemy.util;

import javax.annotation.Nonnull;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import targoss.hardcorealchemy.util.MorphDiet.Needs;

/**
 * An overridable utility class to apply nutrition and thirst changes to the player.
 * This class' API should be considered unstable.
 */
public class NutritionExtension implements INutritionExtension {
    public static INutritionExtension INSTANCE = new NutritionExtension();
    
    public void restoreNutrient(EntityPlayer player, String nutrientName, float amount) { }
    
    public void addGrassToFoodHistory(EntityPlayer player) { }
    
    public void restoreThirst(EntityPlayer player, MorphDiet.Needs needs, int thirstSustain, int thirstSaturationSustain) { }
    
    public int drinkWater(EntityPlayer player, MorphDiet.Needs needs, BlockPos pos, IBlockState blockState, int thirstSustain, int thirstSaturationSustain) {
        return INutritionExtension.Success.NONE;
    }
    
    public boolean isItemDrinkable(Item item) { return false; }
    
    /**
     * Get which nutritional needs and restrictions are enabled for this player
     */
    public @Nonnull Needs getNeeds(EntityPlayer player) {
        return MorphDiet.PLAYER_NEEDS;
    }
}
