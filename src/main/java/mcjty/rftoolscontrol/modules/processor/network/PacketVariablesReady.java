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
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public record PacketVariablesReady(@Nullable BlockPos pos, String command, List<Parameter> list) implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(RFToolsControl.MODID, "variables_ready");
    public static final CustomPacketPayload.Type<PacketVariablesReady> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketVariablesReady> CODEC = StreamCodec.of(
            (buf, packet) -> {
                if (packet.pos != null) {
                    buf.writeBoolean(true);
                    buf.writeBlockPos(packet.pos);
                } else {
                    buf.writeBoolean(false);
                }
                buf.writeUtf(packet.command);
                buf.writeInt(packet.list.size());
                for (Parameter parameter : packet.list) {
                    if (parameter == null) {
                        buf.writeByte(-1);
                    } else {
                        ParameterTools.writeToBuf(buf, parameter);
                    }
                }
            },
            buf -> {
                BlockPos pos = buf.readBoolean() ? buf.readBlockPos() : null;
                String command = buf.readUtf(32767);
                int size = buf.readInt();
                List<Parameter> list = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    list.add(ParameterTools.readFromBuf(buf));
                }
                return new PacketVariablesReady(pos, command, list);
            }
    );

    public PacketVariablesReady(@Nullable BlockPos pos, String command, List<Parameter> list) {
        this.pos = pos;
        this.command = command;
        this.list = new ArrayList<>();
        this.list.addAll(list);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
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
