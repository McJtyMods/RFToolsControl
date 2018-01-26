package mcjty.rftoolscontrol.blocks.programmer;

import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.blocks.GenericRFToolsBlock;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ProgrammerBlock extends GenericRFToolsBlock<ProgrammerTileEntity, ProgrammerContainer> {

    public ProgrammerBlock() {
        super(Material.IRON, ProgrammerTileEntity.class, ProgrammerContainer.class, "programmer", false);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Class<GuiProgrammer> getGuiClass() {
        return GuiProgrammer.class;
    }

    @Override
    public int getGuiID() {
        return RFToolsControl.GUI_PROGRAMMER;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, World playerIn, List<String> list, ITooltipFlag advanced) {
        super.addInformation(stack, playerIn, list, advanced);
        list.add("Use this block to make programs");
        list.add("on a program card for the programmer");
    }
}
