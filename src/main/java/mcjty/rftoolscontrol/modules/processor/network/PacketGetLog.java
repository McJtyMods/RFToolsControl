package mcjty.rftoolscontrol.modules.processor.network;

import mcjty.lib.network.PacketGetListFromServer;
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
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Supplier;

public class PacketGetLog extends PacketGetListFromServer {

    private final boolean fromTablet;

    public PacketGetLog(FriendlyByteBuf buf) {
        super(buf);
        fromTablet = buf.readBoolean();
    }

    public PacketGetLog(ResourceKey<Level> dimension, BlockPos pos, boolean fromTablet) {
        super(dimension, pos, ProcessorTileEntity.CMD_GETLOG.name(), TypedMap.EMPTY);
        this.fromTablet = fromTablet;
    }

    public PacketGetLog(ResourceKey<Level> dimension, BlockPos pos, String cmd, @Nonnull TypedMap params) {
        super(dimension, pos, cmd, params);
        fromTablet = false;
    }

    public PacketGetLog(BlockPos pos, String cmd, @Nonnull TypedMap params) {
        super(pos, cmd, params);
        fromTablet = false;
    }

    public PacketGetLog(BlockPos pos, String cmd) {
        super(pos, cmd);
        fromTablet = false;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        super.toBytes(buf);
        buf.writeBoolean(fromTablet);
    }

    @Override
    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerLevel world = LevelTools.getLevel(ctx.getSender().getCommandSenderWorld(), dimension);
            if (world.hasChunkAt(pos)) {
                BlockEntity te = world.getBlockEntity(pos);
                if (te instanceof GenericTileEntity) {
                    List<String> list = ((GenericTileEntity) te).executeServerCommandList(ProcessorTileEntity.CMD_GETLOG.name(), ctx.getSender(), params, String.class);
                    if (fromTablet) {
                        // We don't have a good position for our tile entity as it might not exist client-side
                        RFToolsCtrlMessages.INSTANCE.sendTo(new PacketLogReady(null, ProcessorTileEntity.CMD_GETLOG.name(), list), ctx.getSender().connection.connection, NetworkDirection.PLAY_TO_CLIENT);
                    } else {
                        RFToolsCtrlMessages.INSTANCE.sendTo(new PacketLogReady(pos, ProcessorTileEntity.CMD_GETLOG.name(), list), ctx.getSender().connection.connection, NetworkDirection.PLAY_TO_CLIENT);
                    }
                }
            }
        });
        ctx.setPacketHandled(true);
    }
}
