package mcjty.rftoolscontrol.modules.processor.network;

import mcjty.lib.network.TypedMapTools;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.LevelTools;
import mcjty.rftoolscontrol.modules.processor.blocks.ProcessorTileEntity;
import mcjty.rftoolscontrol.setup.RFToolsCtrlMessages;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public class PacketGetLog {

    protected BlockPos pos;
    protected RegistryKey<World> type;
    protected TypedMap params;
    private boolean fromTablet;

    public PacketGetLog(PacketBuffer buf) {
        pos = buf.readBlockPos();
        type = LevelTools.getId(buf.readResourceLocation());
        params = TypedMapTools.readArguments(buf);
        fromTablet = buf.readBoolean();
    }

    public PacketGetLog(RegistryKey<World> type, BlockPos pos, boolean fromTablet) {
        this.pos = pos;
        this.type = type;
        this.params = TypedMap.EMPTY;
        this.fromTablet = fromTablet;
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeBlockPos(pos);
        buf.writeResourceLocation(type.location());
        TypedMapTools.writeArguments(buf, params);
        buf.writeBoolean(fromTablet);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerWorld world = LevelTools.getLevel(ctx.getSender().getCommandSenderWorld(), type);
            if (world.hasChunkAt(pos)) {
                TileEntity te = world.getBlockEntity(pos);
                if (te instanceof GenericTileEntity) {
                    List<String> list = ((GenericTileEntity) te).executeServerCommandList(ProcessorTileEntity.CMD_GETLOG.getName(), ctx.getSender(), params, String.class);
                    if (fromTablet) {
                        // We don't have a good position for our tile entity as it might not exist client-side
                        RFToolsCtrlMessages.INSTANCE.sendTo(new PacketLogReady(null, ProcessorTileEntity.CMD_GETLOG.getName(), list), ctx.getSender().connection.connection, NetworkDirection.PLAY_TO_CLIENT);
                    } else {
                        RFToolsCtrlMessages.INSTANCE.sendTo(new PacketLogReady(pos, ProcessorTileEntity.CMD_GETLOG.getName(), list), ctx.getSender().connection.connection, NetworkDirection.PLAY_TO_CLIENT);
                    }
                }
            }
        });
        ctx.setPacketHandled(true);
    }
}
