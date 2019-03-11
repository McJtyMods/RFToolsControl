package mcjty.rftoolscontrol.blocks.workbench;

import mcjty.lib.gui.GenericGuiContainer;
import mcjty.rftoolscontrol.blocks.GenericRFToolsBlock;
import mcjty.rftoolscontrol.proxy.GuiProxy;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.function.BiFunction;

public class WorkbenchBlock extends GenericRFToolsBlock<WorkbenchTileEntity, WorkbenchContainer> {

    public WorkbenchBlock() {
        super(Material.IRON, WorkbenchTileEntity.class, WorkbenchContainer::new, "workbench", false);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public BiFunction<WorkbenchTileEntity, WorkbenchContainer, GenericGuiContainer<? super WorkbenchTileEntity>> getGuiFactory() {
        return GuiWorkbench::new;
    }

    @Override
    public int getGuiID() {
        return GuiProxy.GUI_WORKBENCH;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, World playerIn, List<String> list, ITooltipFlag advanced) {
        super.addInformation(stack, playerIn, list, advanced);
        list.add("A general workbench that works well");
        list.add("with a processor but can also be");
        list.add("used standalone");
    }
}
