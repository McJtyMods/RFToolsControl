package mcjty.rftoolscontrol.modules.processor.network;


import mcjty.lib.McJtyLib;
import mcjty.lib.network.IClientCommandHandler;
import mcjty.lib.typed.Type;
import mcjty.lib.varia.Logging;
import mcjty.rftoolscontrol.modules.processor.blocks.ProcessorContainer;
import mcjty.rftoolscontrol.modules.processor.logic.Parameter;
import mcjty.rftoolscontrol.modules.processor.logic.ParameterTools;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PacketVariablesReady {

    public BlockPos pos;
    public List<Parameter> list;
    public String command;

    public PacketVariablesReady(PacketBuffer buf) {
        if (buf.readBoolean()) {
            pos = buf.readBlockPos();
        }
        command = buf.readString(32767);

        int size = buf.readInt();
        if (size != -1) {
            list = new ArrayList<>(size);
            for (int i = 0 ; i < size ; i++) {
                list.add(ParameterTools.readFromBuf(buf));
            }
        } else {
            list = null;
        }
    }

    public PacketVariablesReady(@Nullable BlockPos pos, String command, List<Parameter> list) {
        this.pos = pos;
        this.command = command;
        this.list = new ArrayList<>();
        this.list.addAll(list);
    }

    public void toBytes(PacketBuffer buf) {
        if (pos != null) {
            buf.writeBoolean(true);
            buf.writeBlockPos(pos);
        } else {
            buf.writeBoolean(false);
        }
        buf.writeString(command);
        if (list == null) {
            buf.writeInt(-1);
        } else {
            buf.writeInt(list.size());
            for (Parameter item : list) {
                if (item == null) {
                    buf.writeByte(-1);
                } else {
                    ParameterTools.writeToBuf(buf, item);
                }
            }
        }
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            TileEntity te;
            if (pos == null) {
                // We are working from a tablet. Find the tile entity through the open container
                ProcessorContainer container = getOpenContainer();
                if (container == null) {
                    Logging.log("Container is missing!");
                    return;
                }
                te = container.getTe();
            } else {
                te = McJtyLib.proxy.getClientWorld().getTileEntity(pos);
                if (!(te instanceof IClientCommandHandler)) {
                    Logging.log("TileEntity is not a ClientCommandHandler!");
                    return;
                }
            }
            IClientCommandHandler clientCommandHandler = (IClientCommandHandler) te;
            if (!clientCommandHandler.receiveListFromServer(command, list, Type.create(Parameter.class))) {
                Logging.log("Command " + command + " was not handled!");
            }
        });
        ctx.setPacketHandled(true);
    }

    private static ProcessorContainer getOpenContainer() {
        Container container = McJtyLib.proxy.getClientPlayer().openContainer;
        if (container instanceof ProcessorContainer) {
            return (ProcessorContainer) container;
        } else {
            return null;
        }
    }

}
