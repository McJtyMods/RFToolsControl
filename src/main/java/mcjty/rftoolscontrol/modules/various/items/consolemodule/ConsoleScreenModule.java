package mcjty.rftoolscontrol.modules.various.items.consolemodule;

import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.WorldTools;
import mcjty.rftoolsbase.api.screens.IScreenDataHelper;
import mcjty.rftoolsbase.api.screens.IScreenModule;
import mcjty.rftoolscontrol.compat.rftoolssupport.ModuleDataLog;
import mcjty.rftoolscontrol.modules.processor.ProcessorSetup;
import mcjty.rftoolscontrol.modules.processor.blocks.ProcessorTileEntity;
import mcjty.rftoolscontrol.setup.Config;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

import java.util.List;

public class ConsoleScreenModule implements IScreenModule<ModuleDataLog> {
    private DimensionType dim = DimensionType.OVERWORLD;
    private BlockPos coordinate = BlockPosTools.INVALID;

    @Override
    public ModuleDataLog getData(IScreenDataHelper h, World worldObj, long millis) {
        World world = WorldTools.getWorld(worldObj, dim);
        if (world == null) {
            return null;
        }

        if (!WorldTools.isLoaded(world, coordinate)) {
            return null;
        }

        Block block = world.getBlockState(coordinate).getBlock();
        if (block != ProcessorSetup.PROCESSOR.get()) {
            return null;
        }

        TileEntity te = world.getTileEntity(coordinate);
        if (te instanceof ProcessorTileEntity) {
            ProcessorTileEntity processor = (ProcessorTileEntity) te;
            List<String> lastMessages = processor.getLastMessages(12);
            return new ModuleDataLog(lastMessages);
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
        return Config.CONSOLEMODULE_RFPERTICK.get();
    }

    @Override
    public void mouseClick(World world, int x, int y, boolean clicked, PlayerEntity player) {
    }
}
