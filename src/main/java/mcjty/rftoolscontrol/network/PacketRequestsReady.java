package mcjty.rftoolscontrol.network;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.IClientCommandHandler;
import mcjty.lib.network.NetworkTools;

import mcjty.lib.typed.Type;
import mcjty.lib.varia.Logging;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.blocks.craftingstation.CraftingRequest;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;


import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PacketRequestsReady implements IMessage {

    public BlockPos pos;
    public List<CraftingRequest> list;
    public String command;

    public PacketRequestsReady() {
    }

    public PacketRequestsReady(ByteBuf buf) {
        fromBytes(buf);
    }

    public PacketRequestsReady(BlockPos pos, String command, List<CraftingRequest> list) {
        this.pos = pos;
        this.command = command;
        this.list = new ArrayList<>();
        this.list.addAll(list);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = NetworkTools.readPos(buf);
        command = NetworkTools.readString(buf);

        int size = buf.readInt();
        if (size != -1) {
            list = new ArrayList<>(size);
            for (int i = 0 ; i < size ; i++) {
                String id = NetworkTools.readString(buf);
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

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkTools.writePos(buf, pos);

        NetworkTools.writeString(buf, command);

        if (list == null) {
            buf.writeInt(-1);
        } else {
            buf.writeInt(list.size());
            for (CraftingRequest item : list) {
                NetworkTools.writeString(buf, item.getTicket());
                NetworkTools.writeItemStack(buf, item.getStack());
                buf.writeInt(item.getTodo());
                buf.writeLong(item.getOk());
                buf.writeLong(item.getFailed());
            }
        }
    }

    public void handle(Supplier<Context> supplier) {
        Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            TileEntity te = RFToolsControl.proxy.getClientWorld().getTileEntity(pos);
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
