package mcjty.rftoolscontrol.modules.multitank.blocks;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import mcjty.rftoolscontrol.compat.RFToolsControlTOPDriver;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;

import javax.annotation.Nonnull;

import static mcjty.lib.builder.TooltipBuilder.header;
import static mcjty.lib.builder.TooltipBuilder.key;

public class MultiTankBlock extends BaseBlock {

    public static final VoxelShape SMALLER_CUBE = Shapes.box(0.01f, 0.01f, 0.01f, 0.99f, 0.99f, 0.99f);

    public MultiTankBlock() {
        super(new BlockBuilder()
                .topDriver(RFToolsControlTOPDriver.DRIVER)
                .info(key("message.rftoolscontrol.shiftmessage"))
                .infoShift(header())
                .tileEntitySupplier(MultiTankTileEntity::new));
    }

    @Override
    public RotationType getRotationType() {
        return RotationType.NONE;
    }

    @Nonnull
    @Override
    public VoxelShape getOcclusionShape(@Nonnull BlockState state, @Nonnull BlockGetter world, @Nonnull BlockPos pos) {
        return SMALLER_CUBE;
    }
}
