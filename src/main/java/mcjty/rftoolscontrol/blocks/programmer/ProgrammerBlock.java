package mcjty.rftoolscontrol.blocks.programmer;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockBuilder;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;
import java.util.List;

public class ProgrammerBlock extends BaseBlock {

    public ProgrammerBlock() {
        super(new BlockBuilder()
                .tileEntitySupplier(ProgrammerTileEntity::new));
        // @todo 1.15
//        setNeedsRedstoneCheck(true);
    }

//    @Override
//    public BiFunction<ProgrammerTileEntity, ProgrammerContainer, GenericGuiContainer<? super ProgrammerTileEntity>> getGuiFactory() {
//        return GuiProgrammer::new;
//    }


    @Override
    public void addInformation(ItemStack stack, @Nullable IBlockReader world, List<ITextComponent> list, ITooltipFlag advanced) {
        super.addInformation(stack, world, list, advanced);
        list.add(new StringTextComponent("Use this block to make programs"));
        list.add(new StringTextComponent("on a program card for the processor"));
    }
}
