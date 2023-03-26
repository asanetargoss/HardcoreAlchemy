package targoss.hardcorealchemy.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;

public class IndexedGuiHandler {
    public final IGuiHandler handler;
    public int index;
    public ResourceLocation id;
    
    public IndexedGuiHandler(IGuiHandler handler) {
        this.handler = handler;
    }

    public void open(EntityPlayer player, World world, int x, int y, int z) {
        FMLNetworkHandler.openGui(player, id.getResourceDomain(), index, world, x, y, z);
    }
}
