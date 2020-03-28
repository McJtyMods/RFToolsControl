package mcjty.rftoolscontrol.blocks.multitank;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;
import java.util.List;

public class MultiTankBlock extends BaseBlock {

    public static VoxelShape SMALLER_CUBE = VoxelShapes.create(0.01f, 0.01f, 0.01f, 0.99f, 0.99f, 0.99f);

    public MultiTankBlock() {
        super(new BlockBuilder()
                .tileEntitySupplier(MultiTankTileEntity::new));
    }

    @Override
    public RotationType getRotationType() {
        return RotationType.NONE;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable IBlockReader world, List<ITextComponent> list, ITooltipFlag advanced) {
        super.addInformation(stack, world, list, advanced);
        list.add(new StringTextComponent("This is a tank that has capacity"));
        list.add(new StringTextComponent("for four types of liquids"));
        list.add(new StringTextComponent("This block is meant for the processor"));
        list.add(new StringTextComponent("and cannot otherwise be used directly"));
    }

    @Override
    public VoxelShape getRenderShape(BlockState state, IBlockReader world, BlockPos pos) {
        return SMALLER_CUBE;
    }


    // @todo 1.15
//    @Override
//    public boolean isOpaqueCube(BlockState state) {
//        return false;
//    }
}
