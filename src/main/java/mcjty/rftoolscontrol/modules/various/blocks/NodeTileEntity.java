package mcjty.rftoolscontrol.modules.various.blocks;

import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.blockcommands.Command;
import mcjty.lib.blockcommands.ServerCommand;
import mcjty.lib.tileentity.Cap;
import mcjty.lib.tileentity.CapType;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.Sync;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.modules.processor.blocks.ProcessorTileEntity;
import mcjty.rftoolscontrol.modules.various.VariousModule;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.LazyOptional;

public class NodeTileEntity extends GenericTileEntity {

    private String channel;
    private String node;

    private BlockPos processor = null;

    // Bitmask for all six sides
    private int prevIn = 0;
    private int powerOut[] = new int[]{0, 0, 0, 0, 0, 0};

    @Cap(type = CapType.CONTAINER)
    private final LazyOptional<INamedContainerProvider> screenHandler = LazyOptional.of(() -> new DefaultContainerProvider<NodeContainer>("Node")
            .containerSupplier((windowId, player) -> new NodeContainer(windowId, NodeContainer.CONTAINER_FACTORY.get(), getBlockPos(), NodeTileEntity.this))
            .dataListener(Sync.string(new ResourceLocation(RFToolsControl.MODID, "channel"), this::getChannelName, this::setChannelName))
            .dataListener(Sync.string(new ResourceLocation(RFToolsControl.MODID, "node"), this::getNodeName, this::setNodeName)));

    public NodeTileEntity() {
        super(VariousModule.NODE_TILE.get());
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
                TileEntity te = getLevel().getBlockEntity(processor);
                if (te instanceof ProcessorTileEntity) {
                    ProcessorTileEntity processorTileEntity = (ProcessorTileEntity) te;
                    processorTileEntity.redstoneNodeChange(prevIn, powered, node);
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
    public void read(CompoundNBT tagCompound) {
        super.read(tagCompound);
        prevIn = tagCompound.getInt("prevIn");
        for (int i = 0; i < 6; i++) {
            powerOut[i] = tagCompound.getByte("p" + i);
        }
    }

    @Override
    protected void readInfo(CompoundNBT tagCompound) {
        super.readInfo(tagCompound);
        CompoundNBT info = tagCompound.getCompound("Info");
        channel = info.getString("channel");
        node = info.getString("node");
        processor = BlockPosTools.read(info, "processor");
    }

    @Override
    public CompoundNBT save(CompoundNBT tagCompound) {
        super.save(tagCompound);
        tagCompound.putInt("prevIn", prevIn);
        for (int i = 0; i < 6; i++) {
            tagCompound.putByte("p" + i, (byte) powerOut[i]);
        }
        return tagCompound;
    }

    @Override
    protected void writeInfo(CompoundNBT tagCompound) {
        super.writeInfo(tagCompound);
        CompoundNBT info = getOrCreateInfo(tagCompound);
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

    public static final Key<String> PARAM_NODE = new Key<>("node", Type.STRING);
    public static final Key<String> PARAM_CHANNEL = new Key<>("channel", Type.STRING);
    @ServerCommand
    public static final Command<?> CMD_UPDATE = Command.<NodeTileEntity>create("node.update")
            .buildCommand((te, player, params) -> {
                te.node = params.get(PARAM_NODE);
                te.channel = params.get(PARAM_CHANNEL);
                te.setChanged();
            });
}
