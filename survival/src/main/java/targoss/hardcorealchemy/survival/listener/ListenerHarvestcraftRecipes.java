package targoss.hardcorealchemy.survival.listener;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.pam.harvestcraft.item.PresserRecipes;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.oredict.OreDictionary;
import targoss.hardcorealchemy.ModState;
import targoss.hardcorealchemy.listener.HardcoreAlchemyListener;

public class ListenerHarvestcraftRecipes extends HardcoreAlchemyListener {
    @Optional.Method(modid=ModState.ARS_MAGICA_ID)
    public static void addArsMagicaLogToOredict() {
        Block witchwoodLog = Block.REGISTRY.getObject(new ResourceLocation("arsmagica2:witchwood_log"));
        OreDictionary.registerOre("logWood", witchwoodLog);
    }
    
    @Optional.Method(modid=ModState.HARVESTCRAFT_ID)
    public static void fixHarvestcraftWoodPaperRecipes() {
        Method registerItemRecipeMethod = null;
        try {
            registerItemRecipeMethod = PresserRecipes.class.getDeclaredMethod("registerItemRecipe", Item.class, Item.class, Item.class);
            registerItemRecipeMethod.setAccessible(true);
        } catch (NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
        }
        if (registerItemRecipeMethod != null) {
            Item paper = net.minecraft.init.Items.PAPER;
            try {
                for (ItemStack logStack : OreDictionary.getOres("logWood")) {
                    registerItemRecipeMethod.invoke(null, logStack.getItem(), paper, paper);
                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }
    
    @Override
    public void postInit(FMLPostInitializationEvent event) {
        if (ModState.isArsMagicaLoaded) {
            addArsMagicaLogToOredict();
        }
        if (ModState.isHarvestCraftLoaded) {
            fixHarvestcraftWoodPaperRecipes();
        }
    }
}
