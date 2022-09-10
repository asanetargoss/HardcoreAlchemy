package targoss.hardcorealchemy.creatures.block;

import net.minecraft.nbt.NBTBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import targoss.hardcorealchemy.HardcoreAlchemyCore;

public class TileHeartOfForm extends TileEntity {
    @CapabilityInject(IItemHandler.class)
    public static final Capability<IItemHandler> ITEM_HANDLER_CAPABILITY = null;
    public static final ResourceLocation ITEM_HANDLER_RESOURCE = new ResourceLocation(HardcoreAlchemyCore.MOD_ID, "item_handler");
    public static final int SLOT_MORPH_TARGET = 0;
    public static final int SLOT_TRUE_FORM    = 1;
    public static final int SLOT_COUNT        = 2;
    
    // TODO: If the heart becomes inactive for some reason and the player is not present, then we need to update a world capability. What should happen if a different player other than the owner puts out the flame? I think the owner player should die.
    // TODO: The above implies that attempting to remove either seal from the heart causes the seal to be re-applied instantly. Is that really what we want?
    // TODO: How to check if the inventory has changed?
    // TODO: Another capability for storing state indicating that the heart is active (or we might just use a BlockState for that)
    // TODO: Syncing?
    
    public static class ItemHandlerProvider implements ICapabilitySerializable<NBTBase> {
        IItemHandler instance;
        
        public ItemHandlerProvider() {
            instance = new ItemStackHandler(SLOT_COUNT);
        }

        @Override
        public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
            return capability == ITEM_HANDLER_CAPABILITY;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
            if (capability == ITEM_HANDLER_CAPABILITY) {
                return (T)instance;
            }
            return null;
        }

        @Override
        public NBTBase serializeNBT() {
            return ITEM_HANDLER_CAPABILITY.getStorage().writeNBT(ITEM_HANDLER_CAPABILITY, instance, null);
        }

        @Override
        public void deserializeNBT(NBTBase nbt) {
            ITEM_HANDLER_CAPABILITY.getStorage().readNBT(ITEM_HANDLER_CAPABILITY, instance, null, nbt);
        }
        
    }

    public TileHeartOfForm(World world) {
        setWorld(world);
    }
}
