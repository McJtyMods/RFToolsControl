package mcjty.rftoolscontrol.modules.various.items.interactionmodule;

import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.WorldTools;
import mcjty.rftoolsbase.api.screens.IScreenDataHelper;
import mcjty.rftoolsbase.api.screens.IScreenModule;
import mcjty.rftoolsbase.api.screens.data.IModuleDataBoolean;
import mcjty.rftoolscontrol.modules.processor.ProcessorSetup;
import mcjty.rftoolscontrol.modules.processor.blocks.ProcessorTileEntity;
import mcjty.rftoolscontrol.setup.ConfigSetup;
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

public class InteractionScreenModule implements IScreenModule<IModuleDataBoolean> {
    private DimensionType dim = DimensionType.OVERWORLD;
    private BlockPos coordinate = BlockPosTools.INVALID;
    private String line = "";
    private String signal = "";

    @Override
    public IModuleDataBoolean getData(IScreenDataHelper helper, World worldObj, long millis) {
        return null;
    }

    @Override
    public void setupFromNBT(CompoundNBT tagCompound, DimensionType dim, BlockPos pos) {
        if (tagCompound != null) {
            line = tagCompound.getString("text");
            signal = tagCompound.getString("signal");
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
        return ConfigSetup.INTERACTMODULE_RFPERTICK.get();
    }

    @Override
    public void mouseClick(World world, int x, int y, boolean clicked, PlayerEntity player) {
        int xoffset;
        if (!line.isEmpty()) {
            xoffset = 80;
        } else {
            xoffset = 5;
        }
        if (x >= xoffset) {
            if (coordinate.getY() != -1) {
                if (!WorldTools.chunkLoaded(world, coordinate)) {
                    return;
                }

                Block block = world.getBlockState(coordinate).getBlock();
                if (block != ProcessorSetup.PROCESSOR.get()) {
                    return;
                }

                if (clicked) {
                    TileEntity te = world.getTileEntity(coordinate);
                    if (te instanceof ProcessorTileEntity) {
                        ProcessorTileEntity processor = (ProcessorTileEntity) te;
                        processor.signal(signal);
                    }
                }
            } else if (player != null) {
                player.sendStatusMessage(new StringTextComponent(TextFormatting.RED + "Module is not linked to a processor!"), false);
            }
        }
    }

}
