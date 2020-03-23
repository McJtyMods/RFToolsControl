package mcjty.rftoolscontrol.blocks.node;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class NodeBlock extends BaseBlock {

    public NodeBlock() {
        super(new BlockBuilder()
                .tileEntitySupplier(NodeTileEntity::new));
    }


//    @Override
//    public BiFunction<NodeTileEntity, EmptyContainer, GenericGuiContainer<? super NodeTileEntity>> getGuiFactory() {
//        return GuiNode::new;
//    }

//    @Override
//    public boolean needsRedstoneCheck() {
//        return true;
//    }


    @Override
    public void addInformation(ItemStack stack, @Nullable IBlockReader world, List<ITextComponent> list, ITooltipFlag advanced) {
        super.addInformation(stack, world, list, advanced);
        list.add(new StringTextComponent("This node can be remotely accessed"));
        list.add(new StringTextComponent("by the processor that has a network"));
        list.add(new StringTextComponent("card installed"));
    }

    // @todo 1.15
//    @Override
//    @Optional.Method(modid = "theoneprobe")
//    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
//        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
//        TileEntity te = world.getTileEntity(data.getPos());
//        if (te instanceof NodeTileEntity) {
//            NodeTileEntity node = (NodeTileEntity) te;
//            probeInfo.text(TextFormatting.GREEN + "Channel: " + node.getChannelName());
//            probeInfo.text(TextFormatting.GREEN + "Name: " + node.getNodeName());
//        }
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

    // @todo 1.15 is this right?
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
