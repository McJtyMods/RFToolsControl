package mcjty.rftoolscontrol.modules.processor.network;


import mcjty.lib.network.CustomPacketPayload;
import mcjty.lib.network.PlayPayloadContext;
import mcjty.lib.network.TypedMapTools;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.LevelTools;
import mcjty.rftoolsbase.api.control.parameters.Parameter;
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

import java.util.List;

public record PacketGetVariables(ResourceKey<Level> type, BlockPos pos, TypedMap params, Boolean fromTablet) implements CustomPacketPayload {

    public static final ResourceLocation ID = new ResourceLocation(RFToolsControl.MODID, "getvariables");

    public static PacketGetVariables create(FriendlyByteBuf buf) {
        return new PacketGetVariables(LevelTools.getId(buf.readResourceLocation()),
                buf.readBlockPos(),
                TypedMapTools.readArguments(buf),
                buf.readBoolean());
    }

    public static PacketGetVariables create(BlockPos pos, ResourceKey<Level> type, boolean fromTablet) {
        return new PacketGetVariables(type, pos, TypedMap.EMPTY, fromTablet);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeResourceLocation(type.location());
        buf.writeBlockPos(pos);
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
                        List<Parameter> list = ((GenericTileEntity) te).executeServerCommandList(ProcessorTileEntity.CMD_GETVARS.name(), player, params, Parameter.class);
                        RFToolsCtrlMessages.sendToPlayer(new PacketVariablesReady(fromTablet ? null : pos, ProcessorTileEntity.CMD_GETVARS.name(), list), player);
                    }
                }
            });
        });
    }
}
