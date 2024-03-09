package mcjty.rftoolscontrol.modules.processor.network;


import mcjty.lib.blockcommands.ISerializer;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.network.TypedMapTools;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.LevelTools;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.modules.processor.blocks.ProcessorTileEntity;
import mcjty.rftoolscontrol.setup.RFToolsCtrlMessages;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public record PacketGetFluids(BlockPos pos, ResourceKey<Level> type, TypedMap params, boolean fromTablet) implements CustomPacketPayload {

    public static final ResourceLocation ID = new ResourceLocation(RFToolsControl.MODID, "getfluids");

    public static PacketGetFluids create(FriendlyByteBuf buf) {
        return new PacketGetFluids(buf.readBlockPos(),
                LevelTools.getId(buf.readResourceLocation()),
                TypedMapTools.readArguments(buf),
                buf.readBoolean());
    }

    public static PacketGetFluids create(BlockPos pos, ResourceKey<Level> type, boolean fromTablet) {
        return new PacketGetFluids(pos, type, TypedMap.EMPTY, fromTablet);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeResourceLocation(type.location());
        TypedMapTools.writeArguments(buf, params);
        buf.writeBoolean(fromTablet);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public void handle(PlayPayloadContext ctx) {
        ctx.workHandler().submitAsync(() -> {
            ctx.player().ifPresent(player -> {
                ServerLevel world = LevelTools.getLevel(player.getCommandSenderWorld(), type);
                if (world.hasChunkAt(pos)) {
                    BlockEntity te = world.getBlockEntity(pos);
                    if (te instanceof GenericTileEntity) {
                        List<FluidEntry> list = ((GenericTileEntity) te).executeServerCommandList(ProcessorTileEntity.CMD_GETFLUIDS.name(), player, params, FluidEntry.class);
                        RFToolsCtrlMessages.sendToPlayer(new PacketFluidsReady(fromTablet ? null : pos, ProcessorTileEntity.CMD_GETFLUIDS.name(), list), player);
                    }
                }
            });
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
