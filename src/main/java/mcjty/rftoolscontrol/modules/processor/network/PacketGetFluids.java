package mcjty.rftoolscontrol.modules.processor.network;


import mcjty.lib.blockcommands.ISerializer;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.LevelTools;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.modules.processor.blocks.ProcessorTileEntity;
import mcjty.rftoolscontrol.setup.RFToolsCtrlMessages;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public record PacketGetFluids(BlockPos pos, ResourceKey<Level> type, TypedMap params, boolean fromTablet) implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(RFToolsControl.MODID, "getfluids");
    public static final CustomPacketPayload.Type<PacketGetFluids> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketGetFluids> CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeBlockPos(packet.pos);
                buf.writeResourceLocation(packet.type.location());
                TypedMap.STREAM_CODEC.encode(buf, packet.params);
                buf.writeBoolean(packet.fromTablet);
            },
            buf -> new PacketGetFluids(buf.readBlockPos(),
                    LevelTools.getId(buf.readResourceLocation()),
                    TypedMap.STREAM_CODEC.decode(buf),
                    buf.readBoolean())
    );

    public static PacketGetFluids create(BlockPos pos, ResourceKey<Level> type, boolean fromTablet) {
        return new PacketGetFluids(pos, type, TypedMap.EMPTY, fromTablet);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerLevel world = LevelTools.getLevel(ctx.player().getCommandSenderWorld(), type);
            if (world.hasChunkAt(pos)) {
                BlockEntity te = world.getBlockEntity(pos);
                if (te instanceof GenericTileEntity) {
                    List<FluidEntry> list = ((GenericTileEntity) te).executeServerCommandList(ProcessorTileEntity.CMD_GETFLUIDS.name(), ctx.player(), params, FluidEntry.class);
                    RFToolsCtrlMessages.sendToPlayer(new PacketFluidsReady(fromTablet ? null : pos, ProcessorTileEntity.CMD_GETFLUIDS.name(), list), ctx.player());
                }
            }
        });
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
                return new FluidEntry(fluidStack, allocated);
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
