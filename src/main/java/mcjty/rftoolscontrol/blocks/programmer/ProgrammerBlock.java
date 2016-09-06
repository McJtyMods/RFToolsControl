package mcjty.rftoolscontrol.blocks.programmer;

import mcjty.lib.container.GenericGuiContainer;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.blocks.GenericRFToolsBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

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

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> list, boolean advanced) {
        super.addInformation(stack, playerIn, list, advanced);
        list.add("Use this block to make programs");
        list.add("on a program card for the programmer");
    }
}
