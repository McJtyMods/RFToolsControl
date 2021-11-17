package mcjty.rftoolscontrol.modules.craftingstation.network;


import mcjty.lib.network.TypedMapTools;
import mcjty.lib.tileentity.GenericTileEntity;
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
            World world = ctx.getSender().getCommandSenderWorld();
            if (world.hasChunkAt(pos)) {
                TileEntity te = world.getBlockEntity(pos);
                if (te instanceof GenericTileEntity) {
                    List<CraftingRequest> list = ((GenericTileEntity) te).executeServerCommandList(CraftingStationTileEntity.CMD_GETREQUESTS.getName(), ctx.getSender(), params, CraftingRequest.class);
                    RFToolsCtrlMessages.INSTANCE.sendTo(new PacketRequestsReady(pos, CraftingStationTileEntity.CMD_GETREQUESTS.getName(), list),
                            ctx.getSender().connection.connection, NetworkDirection.PLAY_TO_CLIENT);
                } else {
                    Logging.log("Command is not handled!");
                    return;
                }
            }
        });
        ctx.setPacketHandled(true);
    }
}
