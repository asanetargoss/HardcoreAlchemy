package targoss.hardcorealchemy.tweaks.item;

import static net.minecraft.init.Items.REDSTONE;
import static targoss.hardcorealchemy.item.HcAPotion.BAD_EFFECT;
import static targoss.hardcorealchemy.item.Items.ITEMS;
import static targoss.hardcorealchemy.item.Items.POTIONS;
import static targoss.hardcorealchemy.item.Items.POTION_TYPES;
import static targoss.hardcorealchemy.item.Items.addPotionRecipe;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.IForgeRegistry;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.capability.dimensionhistory.ICapabilityDimensionHistory;
import targoss.hardcorealchemy.item.HcAPotion;
import targoss.hardcorealchemy.registrar.RegistrarPotionType;
import targoss.hardcorealchemy.tweaks.listener.ListenerEntityVoidfade;
import targoss.hardcorealchemy.tweaks.listener.ListenerEntityVoidfade.DimensionHistoryChangedFlag;
import targoss.hardcorealchemy.util.Color;

public class Items {
    public static final Item DIMENSIONAL_FLUX_CRYSTAL = ITEMS.add("dimensional_flux_crystal", new Item());
    
    public static final Potion POTION_VOIDFADE = POTIONS.add("voidfade", new HcAPotion(BAD_EFFECT, new Color(94, 10, 199), 3, false));
    public static final Potion POTION_SLIP = POTIONS.add("slip", new HcAPotion(BAD_EFFECT, new Color(223, 69, 0), 4, false));
    
    public static final PotionType POTION_TYPE_VOIDFADE = POTION_TYPES.add("voidfade", RegistrarPotionType.potionTypeFromPotion(POTION_VOIDFADE, 90*20));
    public static final PotionType POTION_TYPE_VOIDFADE_EXTENDED = POTION_TYPES.add("voidfade_extended", RegistrarPotionType.potionTypeFromPotion(POTION_VOIDFADE, 6*60*20));
    
    public static void registerRecipes() {
        IForgeRegistry<PotionType> potionTypeRegistry = GameRegistry.findRegistry(PotionType.class);
        PotionType awkwardPotion = potionTypeRegistry.getValue(new ResourceLocation("awkward"));
        
        addPotionRecipe(
                awkwardPotion,
                new ItemStack(DIMENSIONAL_FLUX_CRYSTAL),
                POTION_TYPE_VOIDFADE,
                false
                );
        addPotionRecipe(
                POTION_TYPE_VOIDFADE,
                new ItemStack(REDSTONE),
                POTION_TYPE_VOIDFADE_EXTENDED,
                false
                );
    }
    
    public static class ClientSide {
        public static final ResourceLocation PROPERTY_NATIVE_DIMENSION = new ResourceLocation(HardcoreAlchemy.MOD_ID, "native_dimension");
        
        public static void registerSpecialModels() {
            DIMENSIONAL_FLUX_CRYSTAL.addPropertyOverride(PROPERTY_NATIVE_DIMENSION, new IItemPropertyGetter() {
                @Override
                public float apply(ItemStack itemStack, World world, EntityLivingBase entity) {
                    int dimension;
                    if (world == null || world.provider == null) {
                        dimension = -1; // Nether
                    } else {
                        dimension = world.provider.getDimension();
                    }
                    ICapabilityDimensionHistory history = ListenerEntityVoidfade.getOrInitDimensionHistory(itemStack, dimension, DimensionHistoryChangedFlag.IGNORE_CHANGES);
                    if (history == null) {
                        return dimension;
                    }
                    return (float)history.getDimensionHistory().get(0);
                }
            });
        }
    }
}
