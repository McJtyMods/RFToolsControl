package mcjty.rftoolscontrol.blocks.workbench;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.rftoolscontrol.setup.GuiProxy;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiFunction;

public class WorkbenchBlock extends BaseBlock {

    public WorkbenchBlock() {
        super(new BlockBuilder()
                .tileEntitySupplier(WorkbenchTileEntity::new));
    }


//    @Override
//    public BiFunction<WorkbenchTileEntity, WorkbenchContainer, GenericGuiContainer<? super WorkbenchTileEntity>> getGuiFactory() {
//        return GuiWorkbench::new;
//    }


    @Override
    public void addInformation(ItemStack stack, @Nullable IBlockReader world, List<ITextComponent> list, ITooltipFlag advanced) {
        super.addInformation(stack, world, list, advanced);
        list.add(new StringTextComponent("A general workbench that works well"));
        list.add(new StringTextComponent("with a processor but can also be"));
        list.add(new StringTextComponent("used standalone"));
    }
}
