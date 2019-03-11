package mcjty.rftoolscontrol.blocks.programmer;

import mcjty.lib.gui.GenericGuiContainer;
import mcjty.rftoolscontrol.blocks.GenericRFToolsBlock;
import mcjty.rftoolscontrol.setup.GuiProxy;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.function.BiFunction;

public class ProgrammerBlock extends GenericRFToolsBlock<ProgrammerTileEntity, ProgrammerContainer> {

    public ProgrammerBlock() {
        super(Material.IRON, ProgrammerTileEntity.class, ProgrammerContainer::new, "programmer", false);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public BiFunction<ProgrammerTileEntity, ProgrammerContainer, GenericGuiContainer<? super ProgrammerTileEntity>> getGuiFactory() {
        return GuiProgrammer::new;
    }

    @Override
    public int getGuiID() {
        return GuiProxy.GUI_PROGRAMMER;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, World playerIn, List<String> list, ITooltipFlag advanced) {
        super.addInformation(stack, playerIn, list, advanced);
        list.add("Use this block to make programs");
        list.add("on a program card for the programmer");
    }
}
