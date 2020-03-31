package mcjty.rftoolscontrol.modules.craftingstation.network;


import mcjty.lib.network.IClientCommandHandler;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.typed.Type;
import mcjty.lib.varia.Logging;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PacketCraftableItemsReady {

    public BlockPos pos;
    public List<ItemStack> list;
    public String command;

    public PacketCraftableItemsReady(PacketBuffer buf) {
        pos = buf.readBlockPos();
        command = buf.readString(32767);
        list = NetworkTools.readItemStackList(buf);
    }

    public PacketCraftableItemsReady(BlockPos pos, String command, List<ItemStack> list) {
        this.pos = pos;
        this.command = command;
        this.list = new ArrayList<>();
        this.list.addAll(list);
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeBlockPos(pos);
        buf.writeString(command);
        NetworkTools.writeItemStackList(buf, list);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            TileEntity te = ctx.getSender().getEntityWorld().getTileEntity(pos);
            if(!(te instanceof IClientCommandHandler)) {
                Logging.log("TileEntity is not a ClientCommandHandler!");
                return;
            }
            IClientCommandHandler clientCommandHandler = (IClientCommandHandler) te;
            if (!clientCommandHandler.receiveListFromServer(command, list, Type.create(ItemStack.class))) {
                Logging.log("Command " + command + " was not handled!");
            }
        });
        ctx.setPacketHandled(true);
    }
}
