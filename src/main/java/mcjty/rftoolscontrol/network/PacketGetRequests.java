package mcjty.rftoolscontrol.network;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.ICommandHandler;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.network.TypedMapTools;

import mcjty.lib.typed.Type;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.Logging;
import mcjty.rftoolscontrol.blocks.craftingstation.CraftingRequest;
import mcjty.rftoolscontrol.blocks.craftingstation.CraftingStationTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;


import java.util.List;
import java.util.function.Supplier;

public class PacketGetRequests implements IMessage {

    protected BlockPos pos;
    protected TypedMap params;

    public PacketGetRequests() {
    }

    public PacketGetRequests(ByteBuf buf) {
        fromBytes(buf);
    }

    public PacketGetRequests(BlockPos pos) {
        this.pos = pos;
        this.params = TypedMap.EMPTY;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = NetworkTools.readPos(buf);
        params = TypedMapTools.readArguments(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkTools.writePos(buf, pos);
        TypedMapTools.writeArguments(buf, params);
    }

    public void handle(Supplier<Context> supplier) {
        Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            TileEntity te = ctx.getSender().getEntityWorld().getTileEntity(pos);
            if(!(te instanceof ICommandHandler)) {
                Logging.log("TileEntity is not a CommandHandler!");
                return;
            }
            ICommandHandler commandHandler = (ICommandHandler) te;
            List<CraftingRequest> list = commandHandler.executeWithResultList(CraftingStationTileEntity.CMD_GETREQUESTS, params, Type.create(CraftingRequest.class));
            RFToolsCtrlMessages.INSTANCE.sendTo(new PacketRequestsReady(pos, CraftingStationTileEntity.CLIENTCMD_GETREQUESTS, list), ctx.getSender());
        });
        ctx.setPacketHandled(true);
    }
}
