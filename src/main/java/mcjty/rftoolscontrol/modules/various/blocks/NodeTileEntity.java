package mcjty.rftoolscontrol.modules.various.blocks;

import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.bindings.GuiValue;
import mcjty.lib.bindings.Value;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.tileentity.Cap;
import mcjty.lib.tileentity.CapType;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.typed.Type;
import mcjty.rftoolscontrol.modules.processor.blocks.ProcessorTileEntity;
import mcjty.rftoolscontrol.modules.various.VariousModule;
import mcjty.rftoolscontrol.modules.various.data.NodeData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import java.util.function.Function;

import static mcjty.lib.api.container.DefaultContainerProvider.empty;

public class NodeTileEntity extends GenericTileEntity {

    @GuiValue
    public static final Value<NodeTileEntity, String> VALUE_CHANNEL = Value.create("channel", Type.STRING, NodeTileEntity::getChannelName, NodeTileEntity::setChannelName);
    @GuiValue
    public static final Value<NodeTileEntity, String> VALUE_NODE = Value.create("node", Type.STRING, NodeTileEntity::getNodeName, NodeTileEntity::setNodeName);

    // Bitmask for all six sides of incoming redstone
    private int prevIn = 0;
    private final int[] powerOut = new int[]{0, 0, 0, 0, 0, 0};

    @Cap(type = CapType.CONTAINER)
    private static final Function<NodeTileEntity, MenuProvider> SCREEN_CAP = tile -> new DefaultContainerProvider<GenericContainer>("Node")
            .containerSupplier(empty(VariousModule.NODE_CONTAINER, tile))
            .data(VariousModule.NODE_DATA, NodeData.STREAM_CODEC, NodeData.CODEC)
            .setupSync(tile);

    public NodeTileEntity(BlockPos pos, BlockState state) {
        super(VariousModule.NODE.be().get(), pos, state);
    }

    public String getNodeName() {
        NodeData data = getData(VariousModule.NODE_DATA.get());
        return data.node() == null ? "" : data.node();
    }

    public String getChannelName() {
        NodeData data = getData(VariousModule.NODE_DATA.get());
        return data.channel() == null ? "" : data.channel();
    }

    public void setChannelName(String channel) {
        NodeData data = getData(VariousModule.NODE_DATA.get());
        setData(VariousModule.NODE_DATA.get(), data.withChannel(channel));
    }

    public void setNodeName(String node) {
        NodeData data = getData(VariousModule.NODE_DATA.get());
        setData(VariousModule.NODE_DATA.get(), data.withNode(node));
    }

    public BlockPos getProcessor() {
        return getData(VariousModule.NODE_DATA.get()).processor();
    }

    public void setProcessor(BlockPos processor) {
        NodeData data = getData(VariousModule.NODE_DATA.get());
        setData(VariousModule.NODE_DATA.get(), data.withProcessor(processor));
    }

    @Override
    public void setPowerInput(int powered) {
        if (powerLevel != powered) {
            BlockPos processorPos = getProcessor();
            if (processorPos != null && getLevel() != null) {
                BlockEntity te = getLevel().getBlockEntity(processorPos);
                if (te instanceof ProcessorTileEntity processor) {
                    processor.redstoneNodeChange(prevIn, powered, getNodeName());
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
    public void loadAdditional(CompoundTag tagCompound, HolderLookup.Provider provider) {
        super.loadAdditional(tagCompound, provider);
        prevIn = tagCompound.getInt("prevIn");
        for (int i = 0; i < 6; i++) {
            powerOut[i] = tagCompound.getByte("p" + i);
        }
    }

    @Override
    public void saveAdditional(@Nonnull CompoundTag tagCompound, HolderLookup.Provider provider) {
        super.saveAdditional(tagCompound, provider);
        tagCompound.putInt("prevIn", prevIn);
        for (int i = 0; i < 6; i++) {
            tagCompound.putByte("p" + i, (byte) powerOut[i]);
        }
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        builder.set(VariousModule.ITEM_NODE_DATA.get(), getData(VariousModule.NODE_DATA.get()));
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput input) {
        super.applyImplicitComponents(input);
        NodeData data = input.get(VariousModule.ITEM_NODE_DATA.get());
        if (data != null) {
            setData(VariousModule.NODE_DATA.get(), data);
        }
    }
}
