package mcjty.rftoolscontrol.modules.processor.network;


import mcjty.lib.blockcommands.ISerializer;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.network.TypedMapTools;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.LevelTools;
import mcjty.rftoolscontrol.modules.processor.blocks.ProcessorTileEntity;
import mcjty.rftoolscontrol.setup.RFToolsCtrlMessages;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class PacketGetFluids {

    private BlockPos pos;
    private ResourceKey<Level> type;
    private TypedMap params;
    private boolean fromTablet;

    public PacketGetFluids(FriendlyByteBuf buf) {
        pos = buf.readBlockPos();
        type = LevelTools.getId(buf.readResourceLocation());
        params = TypedMapTools.readArguments(buf);
        fromTablet = buf.readBoolean();
    }

    public PacketGetFluids(BlockPos pos, ResourceKey<Level> type, boolean fromTablet) {
        this.pos = pos;
        this.type = type;
        this.params = TypedMap.EMPTY;
        this.fromTablet = fromTablet;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeResourceLocation(type.location());
        TypedMapTools.writeArguments(buf, params);
        buf.writeBoolean(fromTablet);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerLevel world = LevelTools.getLevel(ctx.getSender().getCommandSenderWorld(), type);
            if (world.hasChunkAt(pos)) {
                BlockEntity te = world.getBlockEntity(pos);
                if (te instanceof GenericTileEntity) {
                    List<FluidEntry> list = ((GenericTileEntity) te).executeServerCommandList(ProcessorTileEntity.CMD_GETFLUIDS.name(), ctx.getSender(), params, FluidEntry.class);
                    RFToolsCtrlMessages.INSTANCE.sendTo(new PacketFluidsReady(fromTablet ? null : pos, ProcessorTileEntity.CMD_GETFLUIDS.name(), list), ctx.getSender().connection.connection, NetworkDirection.PLAY_TO_CLIENT);
                }
            }
        });
        ctx.setPacketHandled(true);
    }

    public static class FluidEntry {
        private final FluidStack fluidStack;
        private final boolean allocated;

        public static class Serializer implements ISerializer<FluidEntry> {
            @Override
            public Function<FriendlyByteBuf, FluidEntry> getDeserializer() {
                return FluidEntry::fromPacket;
            }

            @Override
            public BiConsumer<FriendlyByteBuf, FluidEntry> getSerializer() {
                return FluidEntry::toPacket;
            }
        }

        public FluidEntry(FluidStack fluidStack, boolean allocated) {
            this.fluidStack = fluidStack;
            this.allocated = allocated;
        }

        public static FluidEntry fromPacket(FriendlyByteBuf buf) {
            if (buf.readBoolean()) {
                FluidStack fluidStack = null;
                if (buf.readBoolean()) {
                    fluidStack = NetworkTools.readFluidStack(buf);
                }
                boolean allocated = buf.readBoolean();
                PacketGetFluids.FluidEntry item = new PacketGetFluids.FluidEntry(fluidStack, allocated);
                return item;
            } else {
                return null;
            }
        }

        public static void toPacket(FriendlyByteBuf buf, FluidEntry item) {
            if (item == null) {
                buf.writeBoolean(false);
            } else {
                buf.writeBoolean(true);
                if (item.getFluidStack() != null) {
                    buf.writeBoolean(true);
                    NetworkTools.writeFluidStack(buf, item.getFluidStack());
                } else {
                    buf.writeBoolean(false);
                }
                buf.writeBoolean(item.isAllocated());
            }
        }

        public FluidStack getFluidStack() {
            return fluidStack;
        }

        public boolean isAllocated() {
            return allocated;
        }
    }
}
