package mcjty.rftoolscontrol.modules.craftingstation.network;


import mcjty.lib.network.NetworkTools;
import mcjty.lib.tileentity.GenericTileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PacketCraftableItemsReady {

    private BlockPos pos;
    private List<ItemStack> list;
    private String command;

    public PacketCraftableItemsReady(PacketBuffer buf) {
        pos = buf.readBlockPos();
        command = buf.readUtf(32767);
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
        buf.writeUtf(command);
        NetworkTools.writeItemStackList(buf, list);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            GenericTileEntity.executeClientCommandHelper(pos, command, list);
        });
        ctx.setPacketHandled(true);
    }
}
