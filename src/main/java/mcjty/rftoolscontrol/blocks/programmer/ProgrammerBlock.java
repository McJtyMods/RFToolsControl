package mcjty.rftoolscontrol.blocks.programmer;

import mcjty.lib.container.GenericGuiContainer;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.blocks.GenericRFToolsBlock;
import net.minecraft.block.material.Material;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ProgrammerBlock extends GenericRFToolsBlock<ProgrammerTileEntity, ProgrammerContainer> {

    public ProgrammerBlock() {
        super(Material.IRON, ProgrammerTileEntity.class, ProgrammerContainer.class, "programmer", false);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Class<? extends GenericGuiContainer> getGuiClass() {
        return GuiProgrammer.class;
    }

    @Override
    public int getGuiID() {
        return RFToolsControl.GUI_PROGRAMMER;
    }
}
