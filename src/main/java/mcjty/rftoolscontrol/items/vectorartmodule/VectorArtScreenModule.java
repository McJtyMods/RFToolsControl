package mcjty.rftoolscontrol.items.vectorartmodule;

import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.WorldTools;
import mcjty.rftoolsbase.api.screens.IScreenDataHelper;
import mcjty.rftoolsbase.api.screens.IScreenModule;
import mcjty.rftoolsbase.api.control.parameters.Tuple;
import mcjty.rftoolscontrol.blocks.processor.ProcessorTileEntity;
import mcjty.rftoolscontrol.compat.rftoolssupport.ModuleDataVectorArt;
import mcjty.rftoolscontrol.config.ConfigSetup;
import mcjty.rftoolscontrol.setup.Registration;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

public class VectorArtScreenModule implements IScreenModule<ModuleDataVectorArt> {
    private DimensionType dim = DimensionType.OVERWORLD;
    private BlockPos coordinate = BlockPosTools.INVALID;

    @Override
    public ModuleDataVectorArt getData(IScreenDataHelper h, World worldObj, long millis) {
        World world = WorldTools.getWorld(worldObj, dim);
        if (world == null) {
            return null;
        }

        if (!WorldTools.chunkLoaded(world, coordinate)) {
            return null;
        }

        Block block = world.getBlockState(coordinate).getBlock();
        if (block != Registration.PROCESSOR.get()) {
            return null;
        }

        TileEntity te = world.getTileEntity(coordinate);
        if (te instanceof ProcessorTileEntity) {
            ProcessorTileEntity processor = (ProcessorTileEntity) te;
            return new ModuleDataVectorArt(processor.getGfxOps(), processor.getOrderedOps());
        }
        return null;
    }

    @Override
    public void setupFromNBT(CompoundNBT tagCompound, DimensionType dim, BlockPos pos) {
        if (tagCompound != null) {
            coordinate = BlockPosTools.INVALID;
            if (tagCompound.contains("monitorx")) {
                if (tagCompound.contains("monitordim")) {
                    this.dim = DimensionType.byName(new ResourceLocation(tagCompound.getString("monitordim")));
                } else {
                    // Compatibility reasons
                    this.dim = DimensionType.byName(new ResourceLocation(tagCompound.getString("dim")));
                }
                if (dim == this.dim) {
                    BlockPos c = new BlockPos(tagCompound.getInt("monitorx"), tagCompound.getInt("monitory"), tagCompound.getInt("monitorz"));
                    int dx = Math.abs(c.getX() - pos.getX());
                    int dy = Math.abs(c.getY() - pos.getY());
                    int dz = Math.abs(c.getZ() - pos.getZ());
                    if (dx <= 64 && dy <= 64 && dz <= 64) {
                        coordinate = c;
                    }
                }
            }
        }
    }

    @Override
    public int getRfPerTick() {
        return ConfigSetup.VECTORARTMODULE_RFPERTICK.get();
    }

    @Override
    public void mouseClick(World world, int x, int y, boolean clicked, PlayerEntity player) {
        int xoffset = 0;
        if (x >= xoffset) {
            if (coordinate.getY() != -1) {
                if (!WorldTools.chunkLoaded(world, coordinate)) {
                    return;
                }

                Block block = world.getBlockState(coordinate).getBlock();
                if (block != Registration.PROCESSOR.get()) {
                    return;
                }

                if (clicked) {
                    TileEntity te = world.getTileEntity(coordinate);
                    if (te instanceof ProcessorTileEntity) {
                        ProcessorTileEntity processor = (ProcessorTileEntity) te;
                        processor.signal(new Tuple(x, y+7));
                    }
                }
            } else if (player != null) {
                player.sendStatusMessage(new StringTextComponent(TextFormatting.RED + "Module is not linked to a processor!"), false);
            }
        }
    }
}
