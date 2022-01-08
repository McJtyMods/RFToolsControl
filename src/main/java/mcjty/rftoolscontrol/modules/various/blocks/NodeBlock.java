package mcjty.rftoolscontrol.modules.various.blocks;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.rftoolscontrol.compat.RFToolsControlTOPDriver;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static mcjty.lib.builder.TooltipBuilder.header;
import static mcjty.lib.builder.TooltipBuilder.key;

public class NodeBlock extends BaseBlock {

    public NodeBlock() {
        super(new BlockBuilder()
                .topDriver(RFToolsControlTOPDriver.DRIVER)
                .info(key("message.rftoolscontrol.shiftmessage"))
                .infoShift(header())
                .tileEntitySupplier(NodeTileEntity::new));
    }


//    @Override
//    public boolean needsRedstoneCheck() {
//        return true;
//    }

    private int getInputStrength(Level world, BlockPos pos, Direction side) {
        return world.getSignal(pos.relative(side), side);
    }

    @Override
    protected void checkRedstone(Level world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        BlockEntity te = world.getBlockEntity(pos);
        if (state.getBlock() instanceof NodeBlock && te instanceof NodeTileEntity processor) {
            int powered = 0;
            if (getInputStrength(world, pos, Direction.DOWN) > 0) {
                powered += 1;
            }
            if (getInputStrength(world, pos, Direction.UP) > 0) {
                powered += 2;
            }
            if (getInputStrength(world, pos, Direction.NORTH) > 0) {
                powered += 4;
            }
            if (getInputStrength(world, pos, Direction.SOUTH) > 0) {
                powered += 8;
            }
            if (getInputStrength(world, pos, Direction.WEST) > 0) {
                powered += 16;
            }
            if (getInputStrength(world, pos, Direction.EAST) > 0) {
                powered += 32;
            }
            processor.setPowerInput(powered);
        }
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, @Nullable Direction side) {
        return true;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter world, @Nonnull BlockPos pos, @Nonnull Direction side) {
        BlockEntity te = world.getBlockEntity(pos);
        if (state.getBlock() instanceof NodeBlock && te instanceof NodeTileEntity processor) {
            return processor.getPowerOut(side.getOpposite());
        }
        return 0;
    }

}
