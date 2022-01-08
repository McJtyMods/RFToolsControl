package mcjty.rftoolscontrol.modules.processor.network;


import mcjty.lib.network.NetworkTools;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.Logging;
import mcjty.lib.varia.SafeClientTools;
import mcjty.rftoolscontrol.modules.processor.blocks.ProcessorContainer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PacketLogReady {

    private final BlockPos pos;
    private final List<String> list;
    private final String command;

    public PacketLogReady(FriendlyByteBuf buf) {
        if (buf.readBoolean()) {
            pos = buf.readBlockPos();
        } else {
            pos = null;
        }
        command = buf.readUtf(32767);
        list = NetworkTools.readStringList(buf);
    }

    public PacketLogReady(@Nullable BlockPos pos, String command, @Nonnull List<String> list) {
        this.pos = pos;
        this.command = command;
        this.list = new ArrayList<>();
        this.list.addAll(list);
    }

    public void toBytes(FriendlyByteBuf buf) {
        if (pos != null) {
            buf.writeBoolean(true);
            buf.writeBlockPos(pos);
        } else {
            buf.writeBoolean(false);
        }
        buf.writeUtf(command);
        NetworkTools.writeStringList(buf, list);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            BlockEntity te;
            if (pos == null) {
                // We are working from a tablet. Find the tile entity through the open container
                ProcessorContainer container = getOpenContainer();
                if (container == null) {
                    Logging.log("Container is missing!");
                    return;
                }
                te = container.getTe();
            } else {
                te = SafeClientTools.getClientWorld().getBlockEntity(pos);
            }
            if (te instanceof GenericTileEntity) {
                ((GenericTileEntity) te).handleListFromServer(command, SafeClientTools.getClientPlayer(), TypedMap.EMPTY, list);
            }
        });
        ctx.setPacketHandled(true);
    }

    private static ProcessorContainer getOpenContainer() {
        AbstractContainerMenu container = SafeClientTools.getClientPlayer().containerMenu;
        if (container instanceof ProcessorContainer) {
            return (ProcessorContainer) container;
        } else {
            return null;
        }
    }
}
