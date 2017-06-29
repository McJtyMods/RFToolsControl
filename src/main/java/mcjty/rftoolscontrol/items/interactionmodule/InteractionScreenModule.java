package mcjty.rftoolscontrol.items.interactionmodule;

import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.WorldTools;
import mcjty.rftools.api.screens.IScreenDataHelper;
import mcjty.rftools.api.screens.IScreenModule;
import mcjty.rftools.api.screens.data.IModuleDataBoolean;
import mcjty.rftoolscontrol.blocks.ModBlocks;
import mcjty.rftoolscontrol.blocks.processor.ProcessorTileEntity;
import mcjty.rftoolscontrol.config.GeneralConfiguration;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class InteractionScreenModule implements IScreenModule<IModuleDataBoolean> {
    private int dim = 0;
    private BlockPos coordinate = BlockPosTools.INVALID;
    private String line = "";
    private String signal = "";

    @Override
    public IModuleDataBoolean getData(IScreenDataHelper helper, World worldObj, long millis) {
        return null;
    }

    @Override
    public void setupFromNBT(NBTTagCompound tagCompound, int dim, BlockPos pos) {
        if (tagCompound != null) {
            line = tagCompound.getString("text");
            signal = tagCompound.getString("signal");
            coordinate = BlockPosTools.INVALID;
            if (tagCompound.hasKey("monitorx")) {
                if (tagCompound.hasKey("monitordim")) {
                    this.dim = tagCompound.getInteger("monitordim");
                } else {
                    // Compatibility reasons
                    this.dim = tagCompound.getInteger("dim");
                }
                if (dim == this.dim) {
                    BlockPos c = new BlockPos(tagCompound.getInteger("monitorx"), tagCompound.getInteger("monitory"), tagCompound.getInteger("monitorz"));
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
        return GeneralConfiguration.INTERACTMODULE_RFPERTICK;
    }

    @Override
    public void mouseClick(World world, int x, int y, boolean clicked, EntityPlayer player) {
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
                if (block != ModBlocks.processorBlock) {
                    return;
                }

                if (clicked) {
                    TileEntity te = world.getTileEntity(coordinate);
                    if (te instanceof ProcessorTileEntity) {
                        ProcessorTileEntity processor = (ProcessorTileEntity) te;
                        processor.signal(signal);
                    }
                }
            } else {
                if (player != null) {
                    ITextComponent component = new TextComponentString(TextFormatting.RED + "Module is not linked to a processor!");
                    if (player instanceof EntityPlayer) {
                        ((EntityPlayer) player).sendStatusMessage(component, false);
                    } else {
                        player.sendMessage(component);
                    }
                }
            }
        }
    }

}
