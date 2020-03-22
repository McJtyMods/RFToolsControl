package mcjty.rftoolscontrol.blocks.processor;

import mcjty.lib.gui.GenericGuiContainer;
import mcjty.rftoolscontrol.setup.GuiProxy;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.Block;
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

public class ProcessorBlock extends GenericRFToolsBlock<ProcessorTileEntity, ProcessorContainer> {

    @Override
    public boolean needsRedstoneCheck() {
        return true;
    }

    public ProcessorBlock() {
        super(Material.IRON, ProcessorTileEntity.class, ProcessorContainer::new, "processor", false);
    }

    @Override
    public void initModel() {
        ProcessorRenderer.register();
        super.initModel();
    }


    @Override
    public BiFunction<ProcessorTileEntity, ProcessorContainer, GenericGuiContainer<? super ProcessorTileEntity>> getGuiFactory() {
        return GuiProcessor::new;
    }

    @Override
    public int getGuiID() {
        return GuiProxy.GUI_PROCESSOR;
    }


    @Override
    public void addInformation(ItemStack stack, World playerIn, List<String> list, ITooltipFlag advanced) {
        super.addInformation(stack, playerIn, list, advanced);
        list.add("The processor executes programs");
        list.add("for automation");
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
        super.neighborChanged(state, world, pos, blockIn, fromPos);
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof ProcessorTileEntity) {
            ((ProcessorTileEntity) te).markFluidSlotsDirty();
        }
    }

    @Override
    public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor) {
        super.onNeighborChange(world, pos, neighbor);
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof ProcessorTileEntity) {
            ((ProcessorTileEntity) te).markFluidSlotsDirty();
        }
    }

    @Override
    @Optional.Method(modid = "theoneprobe")
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
        TileEntity te = world.getTileEntity(data.getPos());
        if (te instanceof ProcessorTileEntity) {
            ProcessorTileEntity processor = (ProcessorTileEntity) te;
            if (processor.hasNetworkCard()) {
                probeInfo.text(TextFormatting.GREEN + "Channel: " + processor.getChannelName());
                probeInfo.text(TextFormatting.GREEN + "Nodes: " + processor.getNodeCount());
            }
            if (mode == ProbeMode.EXTENDED) {
                List<String> lastMessages = processor.getLastMessages(6);
                if (!lastMessages.isEmpty()) {
                    IProbeInfo v = probeInfo.vertical(probeInfo.defaultLayoutStyle().borderColor(0xffff0000));
                    for (String s : lastMessages) {
                        v.text("    " + s);
                    }
                }
            }
        }
    }


    private int getInputStrength(World world, BlockPos pos, Direction side) {
        return world.getRedstonePower(pos.offset(side), side);
    }

    @Override
    protected void checkRedstone(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        TileEntity te = world.getTileEntity(pos);
        if (state.getBlock() instanceof ProcessorBlock && te instanceof ProcessorTileEntity) {
            ProcessorTileEntity processor = (ProcessorTileEntity)te;
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
        if (state.getBlock() instanceof ProcessorBlock && te instanceof ProcessorTileEntity) {
            ProcessorTileEntity processor = (ProcessorTileEntity) te;
            return processor.getPowerOut(side.getOpposite());
        }
        return 0;
    }

}
