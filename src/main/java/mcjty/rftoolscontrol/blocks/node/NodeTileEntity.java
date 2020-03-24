package mcjty.rftoolscontrol.blocks.node;

import mcjty.lib.api.container.CapabilityContainerProvider;
import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.BlockPosTools;
import mcjty.rftoolscontrol.blocks.processor.ProcessorTileEntity;
import mcjty.rftoolscontrol.setup.Registration;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NodeTileEntity extends GenericTileEntity {

    public static final String CMD_UPDATE = "node.update";
    public static final Key<String> PARAM_NODE = new Key<>("node", Type.STRING);
    public static final Key<String> PARAM_CHANNEL = new Key<>("channel", Type.STRING);

    private String channel;
    private String node;

    private BlockPos processor = null;

    // Bitmask for all six sides
    private int prevIn = 0;
    private int powerOut[] = new int[] { 0, 0, 0, 0, 0, 0 };

    private LazyOptional<INamedContainerProvider> screenHandler = LazyOptional.of(() -> new DefaultContainerProvider<NodeContainer>("Node")
            .containerSupplier((windowId,player) -> new NodeContainer(windowId, NodeContainer.CONTAINER_FACTORY, getPos(), NodeTileEntity.this)));

    public NodeTileEntity() {
        super(Registration.NODE_TILE.get());
    }

    public String getNodeName() {
        return node;
    }

    public String getChannelName() {
        return channel;
    }

    public BlockPos getProcessor() {
        return processor;
    }

    public void setProcessor(BlockPos processor) {
        this.processor = processor;
        markDirty();
    }

    @Override
    public void setPowerInput(int powered) {
        if (powerLevel != powered) {
            if (processor != null) {
                TileEntity te = getWorld().getTileEntity(processor);
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
        markDirty();
        getWorld().neighborChanged(this.pos.offset(side), this.getBlockState().getBlock(), this.pos);
    }

    @Override
    public void read(CompoundNBT tagCompound) {
        super.read(tagCompound);
        prevIn = tagCompound.getInt("prevIn");
        for (int i = 0 ; i < 6 ; i++) {
            powerOut[i] = tagCompound.getByte("p" + i);
        }
        readRestorableFromNBT(tagCompound);
    }

    @Override
    public CompoundNBT write(CompoundNBT tagCompound) {
        super.write(tagCompound);
        tagCompound.putInt("prevIn", prevIn);
        for (int i = 0 ; i < 6 ; i++) {
            tagCompound.putByte("p" + i, (byte) powerOut[i]);
        }
        writeRestorableToNBT(tagCompound);
        return tagCompound;
    }

    // @todo 1.15 loot tables
    public void readRestorableFromNBT(CompoundNBT tagCompound) {
        channel = tagCompound.getString("channel");
        node = tagCompound.getString("node");
        processor = BlockPosTools.read(tagCompound, "processor");
    }

    // @todo 1.15 loot tables
    public void writeRestorableToNBT(CompoundNBT tagCompound) {
        if (channel != null) {
            tagCompound.putString("channel", channel);
        }
        if (node != null) {
            tagCompound.putString("node", node);
        }
        if (processor != null) {
            BlockPosTools.write(tagCompound, "processor", processor);
        }
    }

    @Override
    public boolean execute(PlayerEntity playerMP, String command, TypedMap args) {
        boolean rc = super.execute(playerMP, command, args);
        if (rc) {
            return true;
        }
        if (CMD_UPDATE.equals(command)) {
            this.node = args.get(PARAM_NODE);
            this.channel = args.get(PARAM_CHANNEL);
            markDirtyClient();
            return true;
        }
        return false;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction facing) {
        if (cap == CapabilityContainerProvider.CONTAINER_PROVIDER_CAPABILITY) {
            return screenHandler.cast();
        }
        return super.getCapability(cap, facing);
    }

}
