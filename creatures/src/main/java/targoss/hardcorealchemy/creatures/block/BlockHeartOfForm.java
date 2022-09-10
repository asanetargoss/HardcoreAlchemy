package targoss.hardcorealchemy.creatures.block;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

// TODO: See TileEntityEnderChestRenderer
// TODO: See https://wiki.mcjty.eu/modding/index.php?title=Render_Block_TESR_/_OBJ-1.9
public class BlockHeartOfForm extends Block implements ITileEntityProvider {
    public BlockHeartOfForm() {
        super(Material.IRON);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileHeartOfForm(world);
    }
    
    public static class Container extends net.minecraft.inventory.Container {
        @Override
        public boolean canInteractWith(EntityPlayer player) {
            return true;
        }
    }
}
