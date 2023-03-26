package targoss.hardcorealchemy.creatures.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import targoss.hardcorealchemy.creatures.block.TileHumanityPhylactery;

@SideOnly(Side.CLIENT)
public class GuiHumanityPhylactery extends GuiContainer {
    protected final TileHumanityPhylactery te;

    public GuiHumanityPhylactery(Container container, TileHumanityPhylactery te) {
        super(container);
        this.te = te;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        // TODO Auto-generated method stub

    }

}
