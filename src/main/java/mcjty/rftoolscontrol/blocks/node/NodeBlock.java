package mcjty.rftoolscontrol.blocks.node;

import mcjty.lib.container.EmptyContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.rftoolscontrol.setup.GuiProxy;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;

import net.minecraft.world.World;


import java.util.List;
import java.util.function.BiFunction;

public class NodeBlock extends GenericRFToolsBlock<NodeTileEntity, EmptyContainer> {

    public NodeBlock() {
        super(Material.IRON, NodeTileEntity.class, EmptyContainer::new, "node", false);
    }


    @Override
    public BiFunction<NodeTileEntity, EmptyContainer, GenericGuiContainer<? super NodeTileEntity>> getGuiFactory() {
        return GuiNode::new;
    }

    @Override
    public int getGuiID() {
        return GuiProxy.GUI_NODE;
    }

    @Override
    public boolean needsRedstoneCheck() {
        return true;
    }


    @Override
    public void addInformation(ItemStack stack, World playerIn, List<String> list, ITooltipFlag advanced) {
        super.addInformation(stack, playerIn, list, advanced);
        list.add("This node can be remotely accessed");
        list.add("by the processor that has a network");
        list.add("card installed");
    }

    @Override
    @Optional.Method(modid = "theoneprobe")
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
        TileEntity te = world.getTileEntity(data.getPos());
        if (te instanceof NodeTileEntity) {
            NodeTileEntity node = (NodeTileEntity) te;
            probeInfo.text(TextFormatting.GREEN + "Channel: " + node.getChannelName());
            probeInfo.text(TextFormatting.GREEN + "Name: " + node.getNodeName());
        }
    }


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
    public boolean canConnectRedstone(BlockState state, IBlockAccess world, BlockPos pos, Direction side) {
        return true;
    }

    @Override
    protected int getRedstoneOutput(BlockState state, IBlockAccess world, BlockPos pos, Direction side) {
        TileEntity te = world.getTileEntity(pos);
        if (state.getBlock() instanceof NodeBlock && te instanceof NodeTileEntity) {
            NodeTileEntity processor = (NodeTileEntity) te;
            return processor.getPowerOut(side.getOpposite());
        }
        return 0;
    }

}
