package mcjty.rftoolscontrol.modules.processor.network;


import java.util.List;
import mcjty.lib.network.PayloadContext;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.LevelTools;
import mcjty.rftoolsbase.api.control.parameters.Parameter;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.modules.processor.blocks.ProcessorTileEntity;
import mcjty.rftoolscontrol.setup.RFToolsCtrlMessages;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public record PacketGetVariables(ResourceKey<Level> level, BlockPos pos, TypedMap params, Boolean fromTablet) implements CustomPacketPayload {

    public static final Identifier ID = Identifier.fromNamespaceAndPath(RFToolsControl.MODID, "getvariables");
    public static final CustomPacketPayload.Type<PacketGetVariables> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketGetVariables> CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeIdentifier(packet.level.identifier());
                buf.writeBlockPos(packet.pos);
                TypedMap.STREAM_CODEC.encode(buf, packet.params);
                buf.writeBoolean(packet.fromTablet);
            },
            buf -> new PacketGetVariables(LevelTools.getId(buf.readIdentifier()),
                    buf.readBlockPos(),
                    TypedMap.STREAM_CODEC.decode(buf),
                    buf.readBoolean())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static PacketGetVariables create(BlockPos pos, ResourceKey<Level> type, boolean fromTablet) {
        return new PacketGetVariables(type, pos, TypedMap.EMPTY, fromTablet);
    }

    public void handle(PayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerLevel world = LevelTools.getLevel(ctx.player().level(), level);
            if (world.hasChunkAt(pos)) {
                BlockEntity te = world.getBlockEntity(pos);
                if (te instanceof GenericTileEntity) {
                    List<Parameter> list = ((GenericTileEntity) te).executeServerCommandList(ProcessorTileEntity.CMD_GETVARS.name(), ctx.player(), params, Parameter.class);
                    RFToolsCtrlMessages.sendToPlayer(new PacketVariablesReady(fromTablet ? null : pos, ProcessorTileEntity.CMD_GETVARS.name(), list), ctx.player());
                }
            }
        });
    }
}
