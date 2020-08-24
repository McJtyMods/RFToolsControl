package mcjty.rftoolscontrol.modules.craftingstation.network;


import mcjty.lib.network.ICommandHandler;
import mcjty.lib.network.TypedMapTools;
import mcjty.lib.typed.Type;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.Logging;
import mcjty.rftoolscontrol.modules.craftingstation.blocks.CraftingStationTileEntity;
import mcjty.rftoolscontrol.modules.craftingstation.util.CraftingRequest;
import mcjty.rftoolscontrol.setup.RFToolsCtrlMessages;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public class PacketGetRequests {

    protected BlockPos pos;
    protected TypedMap params;

    public PacketGetRequests(PacketBuffer buf) {
        pos = buf.readBlockPos();
        params = TypedMapTools.readArguments(buf);
    }

    public PacketGetRequests(BlockPos pos) {
        this.pos = pos;
        this.params = TypedMap.EMPTY;
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeBlockPos(pos);
        TypedMapTools.writeArguments(buf, params);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            World world = ctx.getSender().getEntityWorld();
            if (world.isBlockLoaded(pos)) {
                TileEntity te = world.getTileEntity(pos);
                if (!(te instanceof ICommandHandler)) {
                    Logging.log("TileEntity is not a CommandHandler!");
                    return;
                }
                ICommandHandler commandHandler = (ICommandHandler) te;
                List<CraftingRequest> list = commandHandler.executeWithResultList(CraftingStationTileEntity.CMD_GETREQUESTS, params, Type.create(CraftingRequest.class));
                RFToolsCtrlMessages.INSTANCE.sendTo(new PacketRequestsReady(pos, CraftingStationTileEntity.CLIENTCMD_GETREQUESTS, list),
                        ctx.getSender().connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
            }
        });
        ctx.setPacketHandled(true);
    }
}
