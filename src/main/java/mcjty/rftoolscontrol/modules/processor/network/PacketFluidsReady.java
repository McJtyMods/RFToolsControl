package mcjty.rftoolscontrol.modules.processor.network;


import mcjty.lib.network.CustomPacketPayload;
import mcjty.lib.network.PlayPayloadContext;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.Logging;
import mcjty.lib.varia.SafeClientTools;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.modules.processor.blocks.ProcessorContainer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public record PacketFluidsReady(@Nullable BlockPos pos, String command, List<PacketGetFluids.FluidEntry> list) implements CustomPacketPayload {

    public static final ResourceLocation ID = new ResourceLocation(RFToolsControl.MODID, "fluidsready");

    public static PacketFluidsReady create(FriendlyByteBuf buf) {
        BlockPos pos = null;
        List<PacketGetFluids.FluidEntry> list;
        if (buf.readBoolean()) {
            pos = buf.readBlockPos();
        }
        String command = buf.readUtf(32767);

        int size = buf.readInt();
        if (size != -1) {
            list = new ArrayList<>(size);
            for (int i = 0 ; i < size ; i++) {
                list.add(PacketGetFluids.FluidEntry.fromPacket(buf));
            }
        } else {
            list = null;
        }
        return new PacketFluidsReady(pos, command, list);
    }

    public PacketFluidsReady(@Nullable BlockPos pos, String command, List<PacketGetFluids.FluidEntry> list) {
        this.pos = pos;
        this.command = command;
        this.list = new ArrayList<>();
        this.list.addAll(list);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
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

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public void handle(PlayPayloadContext ctx) {
        ctx.workHandler().submitAsync(() -> {
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
