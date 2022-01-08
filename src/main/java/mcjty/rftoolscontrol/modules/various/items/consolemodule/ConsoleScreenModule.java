package mcjty.rftoolscontrol.modules.various.items.consolemodule;

import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.LevelTools;
import mcjty.rftoolsbase.api.screens.IScreenDataHelper;
import mcjty.rftoolsbase.api.screens.IScreenModule;
import mcjty.rftoolscontrol.compat.rftoolssupport.ModuleDataLog;
import mcjty.rftoolscontrol.modules.processor.ProcessorModule;
import mcjty.rftoolscontrol.modules.processor.blocks.ProcessorTileEntity;
import mcjty.rftoolscontrol.setup.Config;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Objects;

public class ConsoleScreenModule implements IScreenModule<ModuleDataLog> {
    private ResourceKey<Level> dim = Level.OVERWORLD;
    private BlockPos coordinate = BlockPosTools.INVALID;

    @Override
    public ModuleDataLog getData(IScreenDataHelper h, Level worldObj, long millis) {
        Level world = LevelTools.getLevel(worldObj, dim);
        if (world == null) {
            return null;
        }

        if (!LevelTools.isLoaded(world, coordinate)) {
            return null;
        }

        Block block = world.getBlockState(coordinate).getBlock();
        if (block != ProcessorModule.PROCESSOR.get()) {
            return null;
        }

        BlockEntity te = world.getBlockEntity(coordinate);
        if (te instanceof ProcessorTileEntity processor) {
            List<String> lastMessages = processor.getLastMessages(12);
            return new ModuleDataLog(lastMessages);
        }
        return null;
    }

    @Override
    public void setupFromNBT(CompoundTag tagCompound, ResourceKey<Level> dim, BlockPos pos) {
        if (tagCompound != null) {
            coordinate = BlockPosTools.INVALID;
            if (tagCompound.contains("monitorx")) {
                if (tagCompound.contains("monitordim")) {
                    this.dim = LevelTools.getId(tagCompound.getString("monitordim"));
                } else {
                    // Compatibility reasons
                    this.dim = LevelTools.getId(tagCompound.getString("dim"));
                }
                if (Objects.equals(dim, this.dim)) {
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
    public void mouseClick(Level world, int x, int y, boolean clicked, Player player) {
    }
}
