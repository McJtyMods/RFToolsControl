package mcjty.rftoolscontrol.blocks.node;

import mcjty.lib.container.EmptyContainer;
import mcjty.lib.container.GenericGuiContainer;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.blocks.GenericRFToolsBlock;
import mcjty.rftoolscontrol.blocks.processor.GuiProcessor;
import net.minecraft.block.material.Material;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class NodeBlock extends GenericRFToolsBlock<NodeTileEntity, EmptyContainer> {

    public NodeBlock() {
        super(Material.IRON, NodeTileEntity.class, EmptyContainer.class, "node", false);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Class<? extends GenericGuiContainer> getGuiClass() {
        return GuiProcessor.class;
    }

    @Override
    public int getGuiID() {
        return RFToolsControl.GUI_NODE;
    }

}
