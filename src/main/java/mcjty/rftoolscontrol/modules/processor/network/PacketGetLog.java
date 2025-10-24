package mcjty.rftoolscontrol.modules.processor.network;

import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.LevelTools;
import mcjty.lib.varia.SafeClientTools;
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
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;
import java.util.List;

public record PacketGetLog(ResourceKey<Level> dimension, BlockPos pos, String command, TypedMap params, boolean fromTablet) implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(RFToolsControl.MODID, "getlog");
    public static final CustomPacketPayload.Type<PacketGetLog> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketGetLog> CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeResourceLocation(packet.dimension.location());
                buf.writeBlockPos(packet.pos);
                buf.writeUtf(packet.command);
                TypedMap.STREAM_CODEC.encode(buf, packet.params);
                buf.writeBoolean(packet.fromTablet);
            },
            buf -> new PacketGetLog(LevelTools.getId(buf.readResourceLocation()),
                    buf.readBlockPos(),
                    buf.readUtf(),
                    TypedMap.STREAM_CODEC.decode(buf),
                    buf.readBoolean())
    );

    public static PacketGetLog create(ResourceKey<Level> dimension, BlockPos pos, boolean fromTablet) {
        return new PacketGetLog(dimension, pos, ProcessorTileEntity.CMD_GETLOG.name(), TypedMap.EMPTY, fromTablet);
    }

    public static PacketGetLog create(ResourceKey<Level> dimension, BlockPos pos, String cmd, @Nonnull TypedMap params) {
        return new PacketGetLog(dimension, pos, cmd, params, false);
    }

    public static PacketGetLog create(BlockPos pos, String cmd, @Nonnull TypedMap params) {
        return new PacketGetLog(SafeClientTools.getWorld().dimension(), pos, cmd, params, false);
    }

    public static PacketGetLog create(BlockPos pos, String cmd) {
        return new PacketGetLog(SafeClientTools.getWorld().dimension(), pos, cmd, TypedMap.EMPTY, false);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerLevel world = LevelTools.getLevel(ctx.player().getCommandSenderWorld(), dimension);
            if (world.hasChunkAt(pos)) {
                BlockEntity te = world.getBlockEntity(pos);
                if (te instanceof GenericTileEntity) {
                    List<String> list = ((GenericTileEntity) te).executeServerCommandList(ProcessorTileEntity.CMD_GETLOG.name(), ctx.player(), params, String.class);
                    if (fromTablet) {
                        // We don't have a good position for our tile entity as it might not exist client-side
                        RFToolsCtrlMessages.sendToPlayer(new PacketLogReady(null, ProcessorTileEntity.CMD_GETLOG.name(), list), ctx.player());
                    } else {
                        RFToolsCtrlMessages.sendToPlayer(new PacketLogReady(pos, ProcessorTileEntity.CMD_GETLOG.name(), list), ctx.player());
                    }
                }
            }
        });
    }
}
