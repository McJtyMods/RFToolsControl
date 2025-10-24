package mcjty.rftoolscontrol.modules.processor.network;


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
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record PacketGetVariables(ResourceKey<Level> type, BlockPos pos, TypedMap params, Boolean fromTablet) implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(RFToolsControl.MODID, "getvariables");
    public static final CustomPacketPayload.Type<PacketGetVariables> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketGetVariables> CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeResourceLocation(packet.type.location());
                buf.writeBlockPos(packet.pos);
                TypedMap.STREAM_CODEC.encode(buf, packet.params);
                buf.writeBoolean(packet.fromTablet);
            },
            buf -> new PacketGetVariables(LevelTools.getId(buf.readResourceLocation()),
                    buf.readBlockPos(),
                    TypedMap.STREAM_CODEC.decode(buf),
                    buf.readBoolean())
    );

    public static PacketGetVariables create(BlockPos pos, ResourceKey<Level> type, boolean fromTablet) {
        return new PacketGetVariables(type, pos, TypedMap.EMPTY, fromTablet);
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
                    List<Parameter> list = ((GenericTileEntity) te).executeServerCommandList(ProcessorTileEntity.CMD_GETVARS.name(), ctx.player(), params, Parameter.class);
                    RFToolsCtrlMessages.sendToPlayer(new PacketVariablesReady(fromTablet ? null : pos, ProcessorTileEntity.CMD_GETVARS.name(), list), ctx.player());
                }
            }
        });
    }
}
