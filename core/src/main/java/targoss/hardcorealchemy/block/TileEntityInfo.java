package targoss.hardcorealchemy.block;

import net.minecraft.tileentity.TileEntity;

public class TileEntityInfo<T extends TileEntity> {
    public final Class<T> clazz;
    public String name;
    
    public TileEntityInfo(Class<T> clazz) {
        this.clazz = clazz;
    }
}
