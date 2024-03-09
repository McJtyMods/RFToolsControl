package mcjty.rftoolscontrol.modules.processor.network;


import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.Logging;
import mcjty.lib.varia.SafeClientTools;
import mcjty.rftoolsbase.api.control.parameters.Parameter;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.modules.processor.blocks.ProcessorContainer;
import mcjty.rftoolscontrol.modules.processor.logic.ParameterTools;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public record PacketVariablesReady(@Nullable BlockPos pos, String command, List<Parameter> list) implements CustomPacketPayload {

    public static final ResourceLocation ID = new ResourceLocation(RFToolsControl.MODID, "variables_ready");

    public static PacketVariablesReady create(FriendlyByteBuf buf) {
        BlockPos pos = null;
        String command;
        List<Parameter> list;
        if (buf.readBoolean()) {
            pos = buf.readBlockPos();
        }
        command = buf.readUtf(32767);

        int size = buf.readInt();
        if (size != -1) {
            list = new ArrayList<>(size);
            for (int i = 0 ; i < size ; i++) {
                list.add(ParameterTools.readFromBuf(buf));
            }
        } else {
            list = null;
        }
        return new PacketVariablesReady(pos, command, list);
    }

    public PacketVariablesReady(@Nullable BlockPos pos, String command, List<Parameter> list) {
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
            for (Parameter item : list) {
                if (item == null) {
                    buf.writeByte(-1);
                } else {
                    ParameterTools.writeToBuf(buf, item);
                }
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
