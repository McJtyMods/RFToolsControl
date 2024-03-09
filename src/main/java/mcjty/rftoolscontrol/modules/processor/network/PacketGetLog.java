package mcjty.rftoolscontrol.modules.processor.network;

import mcjty.lib.network.TypedMapTools;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.LevelTools;
import mcjty.lib.varia.SafeClientTools;
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

import javax.annotation.Nonnull;
import java.util.List;

public record PacketGetLog(ResourceKey<Level> dimension, BlockPos pos, String command, TypedMap params, boolean fromTablet) implements CustomPacketPayload {

    public static final ResourceLocation ID = new ResourceLocation(RFToolsControl.MODID, "getlog");

    public static PacketGetLog create(FriendlyByteBuf buf) {
        return new PacketGetLog(LevelTools.getId(buf.readResourceLocation()),
                buf.readBlockPos(),
                buf.readUtf(),
                TypedMapTools.readArguments(buf),
                buf.readBoolean());
    }

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
    public void write(FriendlyByteBuf buf) {
        buf.writeResourceLocation(dimension.location());
        buf.writeBlockPos(pos);
        buf.writeUtf(command);
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
                ServerLevel world = LevelTools.getLevel(player.getCommandSenderWorld(), dimension);
                if (world.hasChunkAt(pos)) {
                    BlockEntity te = world.getBlockEntity(pos);
                    if (te instanceof GenericTileEntity) {
                        List<String> list = ((GenericTileEntity) te).executeServerCommandList(ProcessorTileEntity.CMD_GETLOG.name(), player, params, String.class);
                        if (fromTablet) {
                            // We don't have a good position for our tile entity as it might not exist client-side
                            RFToolsCtrlMessages.sendToPlayer(new PacketLogReady(null, ProcessorTileEntity.CMD_GETLOG.name(), list), player);
                        } else {
                            RFToolsCtrlMessages.sendToPlayer(new PacketLogReady(pos, ProcessorTileEntity.CMD_GETLOG.name(), list), player);
                        }
                    }
                }
            });
        });
    }
}
