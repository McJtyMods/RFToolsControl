package mcjty.rftoolscontrol.modules.various.items.interactionmodule;

import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.LevelTools;
import mcjty.rftoolsbase.api.screens.IScreenDataHelper;
import mcjty.rftoolsbase.api.screens.IScreenModule;
import mcjty.rftoolsbase.api.screens.data.IModuleDataBoolean;
import mcjty.rftoolscontrol.modules.processor.ProcessorModule;
import mcjty.rftoolscontrol.modules.processor.blocks.ProcessorTileEntity;
import mcjty.rftoolscontrol.setup.Config;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.Level;

import java.util.Objects;

public class InteractionScreenModule implements IScreenModule<IModuleDataBoolean> {
    private BlockPos coordinate = BlockPosTools.INVALID;
    private String line = "";
    private String signal = "";

    @Override
    public IModuleDataBoolean getData(IScreenDataHelper helper, Level worldObj, long millis) {
        return null;
    }

    @Override
    public void setupFromNBT(CompoundTag tagCompound, ResourceKey<Level> dim, BlockPos pos) {
        if (tagCompound != null) {
            line = tagCompound.getString("text");
            signal = tagCompound.getString("signal");
            coordinate = BlockPosTools.INVALID;
            if (tagCompound.contains("monitorx")) {
                ResourceKey<Level> dim1 = Level.OVERWORLD;
                if (tagCompound.contains("monitordim")) {
                    dim1 = LevelTools.getId(tagCompound.getString("monitordim"));
                } else {
                    // Compatibility reasons
                    dim1 = LevelTools.getId(tagCompound.getString("dim"));
                }
                if (Objects.equals(dim, dim1)) {
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
        return Config.INTERACTMODULE_RFPERTICK.get();
    }

    @Override
    public void mouseClick(Level world, int x, int y, boolean clicked, Player player) {
        int xoffset;
        if (!line.isEmpty()) {
            xoffset = 80;
        } else {
            xoffset = 5;
        }
        if (x >= xoffset) {
            if (coordinate.getY() != -1) {
                if (!LevelTools.isLoaded(world, coordinate)) {
                    return;
                }

                Block block = world.getBlockState(coordinate).getBlock();
                if (block != ProcessorModule.PROCESSOR.get()) {
                    return;
                }

                if (clicked) {
                    BlockEntity te = world.getBlockEntity(coordinate);
                    if (te instanceof ProcessorTileEntity) {
                        ProcessorTileEntity processor = (ProcessorTileEntity) te;
                        processor.signal(signal);
                    }
                }
            } else if (player != null) {
                player.displayClientMessage(new TextComponent(ChatFormatting.RED + "Module is not linked to a processor!"), false);
            }
        }
    }

}
