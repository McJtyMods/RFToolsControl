package mcjty.rftoolscontrol.blocks.processor;

import mcjty.lib.container.GenericGuiContainer;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.blocks.GenericRFToolsBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ProcessorBlock extends GenericRFToolsBlock<ProcessorTileEntity, ProcessorContainer> {

    public static final PropertyBool WORKING = PropertyBool.create("working");

    @Override
    public boolean needsRedstoneCheck() {
        return true;
    }

    public ProcessorBlock() {
        super(Material.IRON, ProcessorTileEntity.class, ProcessorContainer.class, "processor", false);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Class<? extends GenericGuiContainer> getGuiClass() {
        return GuiProcessor.class;
    }

    @Override
    public int getGuiID() {
        return RFToolsControl.GUI_PROCESSOR;
    }

    private int getInputStrength(World world, BlockPos pos, EnumFacing side) {
        return world.getRedstonePower(pos.offset(side), side);
    }

    @Override
    protected void checkRedstone(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        TileEntity te = world.getTileEntity(pos);
        if (state.getBlock() instanceof ProcessorBlock && te instanceof ProcessorTileEntity) {
            ProcessorTileEntity processor = (ProcessorTileEntity)te;
            int powered = 0;
            if (getInputStrength(world, pos, EnumFacing.DOWN) > 0) {
                powered += 1;
            }
            if (getInputStrength(world, pos, EnumFacing.UP) > 0) {
                powered += 2;
            }
            if (getInputStrength(world, pos, EnumFacing.NORTH) > 0) {
                powered += 4;
            }
            if (getInputStrength(world, pos, EnumFacing.SOUTH) > 0) {
                powered += 8;
            }
            if (getInputStrength(world, pos, EnumFacing.WEST) > 0) {
                powered += 16;
            }
            if (getInputStrength(world, pos, EnumFacing.EAST) > 0) {
                powered += 32;
            }
            processor.setPowerInput(powered);
        }
    }

    @Override
    public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        return true;
    }

    @Override
    protected int getRedstoneOutput(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        TileEntity te = world.getTileEntity(pos);
        if (state.getBlock() instanceof ProcessorBlock && te instanceof ProcessorTileEntity) {
            ProcessorTileEntity processor = (ProcessorTileEntity) te;
            return processor.getPowerOut(side.getOpposite());
        }
        return 0;
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        boolean working = false;
        if (te instanceof ProcessorTileEntity) {
            working = ((ProcessorTileEntity)te).isWorking();
        }
        return state.withProperty(WORKING, working);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, WORKING);
    }

}
