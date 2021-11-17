package mcjty.rftoolscontrol.modules.craftingstation.network;


import mcjty.lib.McJtyLib;
import mcjty.lib.network.IClientCommandHandler;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.typed.Type;
import mcjty.lib.varia.Logging;
import mcjty.rftoolscontrol.modules.craftingstation.util.CraftingRequest;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PacketRequestsReady {

    private BlockPos pos;
    private List<CraftingRequest> list;
    private String command;

    public PacketRequestsReady(PacketBuffer buf) {
        pos = buf.readBlockPos();
        command = buf.readUtf(32767);

        int size = buf.readInt();
        if (size != -1) {
            list = new ArrayList<>(size);
            for (int i = 0 ; i < size ; i++) {
                String id = buf.readUtf(32767);
                ItemStack stack = NetworkTools.readItemStack(buf);
                int amount = buf.readInt();
                CraftingRequest request = new CraftingRequest(id, stack, amount);
                request.setOk(buf.readLong());
                request.setFailed(buf.readLong());
                list.add(request);
            }
        } else {
            list = null;
        }
    }

    public PacketRequestsReady(BlockPos pos, String command, List<CraftingRequest> list) {
        this.pos = pos;
        this.command = command;
        this.list = new ArrayList<>();
        this.list.addAll(list);
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeBlockPos(pos);

        buf.writeUtf(command);

        if (list == null) {
            buf.writeInt(-1);
        } else {
            buf.writeInt(list.size());
            for (CraftingRequest item : list) {
                buf.writeUtf(item.getTicket());
                NetworkTools.writeItemStack(buf, item.getStack());
                buf.writeInt(item.getTodo());
                buf.writeLong(item.getOk());
                buf.writeLong(item.getFailed());
            }
        }
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            TileEntity te = McJtyLib.proxy.getClientWorld().getBlockEntity(pos);
            if(!(te instanceof IClientCommandHandler)) {
                Logging.log("TileEntity is not a ClientCommandHandler!");
                return;
            }
            IClientCommandHandler clientCommandHandler = (IClientCommandHandler) te;
            if (!clientCommandHandler.receiveListFromServer(command, list, Type.create(CraftingRequest.class))) {
                Logging.log("Command " + command + " was not handled!");
            }
        });
        ctx.setPacketHandled(true);
    }
}
