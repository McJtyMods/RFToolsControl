package mcjty.rftoolscontrol.blocks.programmer;

import mcjty.lib.gui.GenericGuiContainer;
import mcjty.rftoolscontrol.setup.GuiProxy;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.List;
import java.util.function.BiFunction;

public class ProgrammerBlock extends GenericRFToolsBlock<ProgrammerTileEntity, ProgrammerContainer> {

    public ProgrammerBlock() {
        super(Material.IRON, ProgrammerTileEntity.class, ProgrammerContainer::new, "programmer", false);
        setNeedsRedstoneCheck(true);
    }


    @Override
    public BiFunction<ProgrammerTileEntity, ProgrammerContainer, GenericGuiContainer<? super ProgrammerTileEntity>> getGuiFactory() {
        return GuiProgrammer::new;
    }

    @Override
    public int getGuiID() {
        return GuiProxy.GUI_PROGRAMMER;
    }


    @Override
    public void addInformation(ItemStack stack, World playerIn, List<String> list, ITooltipFlag advanced) {
        super.addInformation(stack, playerIn, list, advanced);
        list.add("Use this block to make programs");
        list.add("on a program card for the processor");
    }
}
