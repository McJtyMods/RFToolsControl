package mcjty.rftoolscontrol.modules.processor.network;


import mcjty.lib.network.TypedMapTools;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.LevelTools;
import mcjty.rftoolsbase.api.control.parameters.Parameter;
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

import java.util.List;
import java.util.function.Supplier;

public class PacketGetVariables {

    private final BlockPos pos;
    private final ResourceKey<Level> type;
    private final TypedMap params;
    private final boolean fromTablet;

    public PacketGetVariables(FriendlyByteBuf buf) {
        pos = buf.readBlockPos();
        type = LevelTools.getId(buf.readResourceLocation());
        params = TypedMapTools.readArguments(buf);
        fromTablet = buf.readBoolean();
    }

    public PacketGetVariables(BlockPos pos, ResourceKey<Level> type, boolean fromTablet) {
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
                    List<Parameter> list = ((GenericTileEntity) te).executeServerCommandList(ProcessorTileEntity.CMD_GETVARS.name(), ctx.getSender(), params, Parameter.class);
                    RFToolsCtrlMessages.INSTANCE.sendTo(new PacketVariablesReady(fromTablet ? null : pos, ProcessorTileEntity.CMD_GETVARS.name(), list),
                            ctx.getSender().connection.connection, NetworkDirection.PLAY_TO_CLIENT);
                }
            }
        });
        ctx.setPacketHandled(true);
    }
}
