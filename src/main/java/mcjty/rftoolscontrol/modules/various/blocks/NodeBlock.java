package mcjty.rftoolscontrol.modules.various.blocks;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.rftoolscontrol.compat.RFToolsControlTOPDriver;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

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

    private int getInputStrength(World world, BlockPos pos, Direction side) {
        return world.getRedstonePower(pos.offset(side), side);
    }

    @Override
    protected void checkRedstone(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        TileEntity te = world.getTileEntity(pos);
        if (state.getBlock() instanceof NodeBlock && te instanceof NodeTileEntity) {
            NodeTileEntity processor = (NodeTileEntity)te;
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
    public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, @Nullable Direction side) {
        return true;
    }

    @Override
    public int getStrongPower(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
        TileEntity te = world.getTileEntity(pos);
        if (state.getBlock() instanceof NodeBlock && te instanceof NodeTileEntity) {
            NodeTileEntity processor = (NodeTileEntity) te;
            return processor.getPowerOut(side.getOpposite());
        }
        return 0;
    }

}
