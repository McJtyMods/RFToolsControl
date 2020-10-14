package mcjty.rftoolscontrol.blocks.programmer;

import mcjty.lib.gui.GenericGuiContainer;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.blocks.GenericRFToolsBlock;
import mcjty.rftoolscontrol.setup.GuiProxy;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

public class ProgrammerBlock extends GenericRFToolsBlock<ProgrammerTileEntity, ProgrammerContainer> {

    public ProgrammerBlock() {
        super(Material.IRON, ProgrammerTileEntity.class, ProgrammerContainer::new, "programmer", false);
        setNeedsRedstoneCheck(true);
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
        list.addAll(Arrays.asList(I18n.format("tooltips." + RFToolsControl.MODID+"."+"programmer").split("0x0a")));
    }
}
