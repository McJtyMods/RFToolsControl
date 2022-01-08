package mcjty.rftoolscontrol.modules.various.blocks;

import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.bindings.GuiValue;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.tileentity.Cap;
import mcjty.lib.tileentity.CapType;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.varia.BlockPosTools;
import mcjty.rftoolscontrol.modules.processor.blocks.ProcessorTileEntity;
import mcjty.rftoolscontrol.modules.various.VariousModule;
import net.minecraft.world.MenuProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;

import static mcjty.lib.api.container.DefaultContainerProvider.empty;

public class NodeTileEntity extends GenericTileEntity {

    @GuiValue
    private String channel;
    @GuiValue
    private String node;

    private BlockPos processor = null;

    // Bitmask for all six sides
    private int prevIn = 0;
    private final int[] powerOut = new int[]{0, 0, 0, 0, 0, 0};

    @Cap(type = CapType.CONTAINER)
    private final LazyOptional<MenuProvider> screenHandler = LazyOptional.of(() -> new DefaultContainerProvider<GenericContainer>("Node")
            .containerSupplier(empty(VariousModule.NODE_CONTAINER, this))
            .setupSync(this));

    public NodeTileEntity(BlockPos pos, BlockState state) {
        super(VariousModule.NODE_TILE.get(), pos, state);
    }

    public String getNodeName() {
        return node;
    }

    public String getChannelName() {
        return channel;
    }

    public void setChannelName(String channel) {
        this.channel = channel;
        setChanged();
    }

    public void setNodeName(String node) {
        this.node = node;
        setChanged();
    }

    public BlockPos getProcessor() {
        return processor;
    }

    public void setProcessor(BlockPos processor) {
        this.processor = processor;
        setChanged();
    }

    @Override
    public void setPowerInput(int powered) {
        if (powerLevel != powered) {
            if (processor != null) {
                BlockEntity te = getLevel().getBlockEntity(processor);
                if (te instanceof ProcessorTileEntity processor) {
                    processor.redstoneNodeChange(prevIn, powered, node);
                }
            }
            prevIn = powered;
        }
        super.setPowerInput(powered);
    }

    public int getPowerOut(Direction side) {
        return powerOut[side.ordinal()];
    }

    public void setPowerOut(Direction side, int powerOut) {
        this.powerOut[side.ordinal()] = powerOut;
        setChanged();
        getLevel().neighborChanged(this.worldPosition.relative(side), this.getBlockState().getBlock(), this.worldPosition);
    }

    @Override
    public void load(CompoundTag tagCompound) {
        super.load(tagCompound);
        prevIn = tagCompound.getInt("prevIn");
        for (int i = 0; i < 6; i++) {
            powerOut[i] = tagCompound.getByte("p" + i);
        }
    }

    @Override
    protected void loadInfo(CompoundTag tagCompound) {
        super.loadInfo(tagCompound);
        CompoundTag info = tagCompound.getCompound("Info");
        channel = info.getString("channel");
        node = info.getString("node");
        processor = BlockPosTools.read(info, "processor");
    }

    @Override
    public void saveAdditional(@Nonnull CompoundTag tagCompound) {
        super.saveAdditional(tagCompound);
        tagCompound.putInt("prevIn", prevIn);
        for (int i = 0; i < 6; i++) {
            tagCompound.putByte("p" + i, (byte) powerOut[i]);
        }
    }

    @Override
    protected void saveInfo(CompoundTag tagCompound) {
        super.saveInfo(tagCompound);
        CompoundTag info = getOrCreateInfo(tagCompound);
        if (channel != null) {
            info.putString("channel", channel);
        }
        if (node != null) {
            info.putString("node", node);
        }
        if (processor != null) {
            BlockPosTools.write(info, "processor", processor);
        }
    }
}
