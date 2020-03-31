package mcjty.rftoolscontrol.modules.processor.network;


import mcjty.lib.McJtyLib;
import mcjty.lib.network.IClientCommandHandler;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.typed.Type;
import mcjty.lib.varia.Logging;
import mcjty.rftoolscontrol.modules.processor.network.PacketGetFluids;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PacketFluidsReady {

    public BlockPos pos;
    public List<PacketGetFluids.FluidEntry> list;
    public String command;

    public PacketFluidsReady(PacketBuffer buf) {
        pos = buf.readBlockPos();
        command = buf.readString(32767);

        int size = buf.readInt();
        if (size != -1) {
            list = new ArrayList<>(size);
            for (int i = 0 ; i < size ; i++) {
                FluidStack fluidStack = null;
                if (buf.readBoolean()) {
                    fluidStack = NetworkTools.readFluidStack(buf);
                }
                boolean allocated = buf.readBoolean();
                PacketGetFluids.FluidEntry item = new PacketGetFluids.FluidEntry(fluidStack, allocated);
                list.add(item);
            }
        } else {
            list = null;
        }
    }

    public PacketFluidsReady(BlockPos pos, String command, List<PacketGetFluids.FluidEntry> list) {
        this.pos = pos;
        this.command = command;
        this.list = new ArrayList<>();
        this.list.addAll(list);
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeBlockPos(pos);

        buf.writeString(command);

        if (list == null) {
            buf.writeInt(-1);
        } else {
            buf.writeInt(list.size());
            for (PacketGetFluids.FluidEntry item : list) {
                if (item == null) {
                    buf.writeByte(-1);
                } else {
                    if (item.getFluidStack() != null) {
                        buf.writeBoolean(true);
                        NetworkTools.writeFluidStack(buf, item.getFluidStack());
                    } else {
                        buf.writeBoolean(false);
                    }
                    buf.writeBoolean(item.isAllocated());
                }
            }
        }
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            TileEntity te = McJtyLib.proxy.getClientWorld().getTileEntity(pos);
            if(!(te instanceof IClientCommandHandler)) {
                Logging.log("TileEntity is not a ClientCommandHandler!");
                return;
            }
            IClientCommandHandler clientCommandHandler = (IClientCommandHandler) te;
            if (!clientCommandHandler.receiveListFromServer(command, list, Type.create(PacketGetFluids.FluidEntry.class))) {
                Logging.log("Command " + command + " was not handled!");
            }
        });
        ctx.setPacketHandled(true);
    }
}
