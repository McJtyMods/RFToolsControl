package mcjty.rftoolscontrol.modules.processor.network;


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

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PacketFluidsReady {

    private BlockPos pos;
    private List<PacketGetFluids.FluidEntry> list;
    private String command;

    public PacketFluidsReady(FriendlyByteBuf buf) {
        if (buf.readBoolean()) {
            pos = buf.readBlockPos();
        }
        command = buf.readUtf(32767);

        int size = buf.readInt();
        if (size != -1) {
            list = new ArrayList<>(size);
            for (int i = 0 ; i < size ; i++) {
                list.add(PacketGetFluids.FluidEntry.fromPacket(buf));
            }
        } else {
            list = null;
        }
    }

    public PacketFluidsReady(@Nullable BlockPos pos, String command, List<PacketGetFluids.FluidEntry> list) {
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

        if (list == null) {
            buf.writeInt(-1);
        } else {
            buf.writeInt(list.size());
            for (PacketGetFluids.FluidEntry item : list) {
                PacketGetFluids.FluidEntry.toPacket(buf, item);
            }
        }
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
