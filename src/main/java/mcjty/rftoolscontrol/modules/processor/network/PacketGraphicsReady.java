package mcjty.rftoolscontrol.modules.processor.network;


import mcjty.lib.varia.SafeClientTools;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.modules.processor.blocks.ProcessorTileEntity;
import mcjty.rftoolscontrol.modules.processor.vectorart.GfxOp;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record PacketGraphicsReady(BlockPos pos, Map<String, GfxOp> gfxOps, List<String> orderedOps) implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(RFToolsControl.MODID, "graphics_ready");
    public static final CustomPacketPayload.Type<PacketGraphicsReady> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketGraphicsReady> CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeBlockPos(packet.pos);
                buf.writeInt(packet.gfxOps.size());
                packet.gfxOps.forEach((key, value) -> {
                    buf.writeUtf(key);
                    value.writeToBuf(buf);
                });
                buf.writeInt(packet.orderedOps.size());
                packet.orderedOps.forEach(buf::writeUtf);
            },
            buf -> {
                BlockPos pos = buf.readBlockPos();
                int size = buf.readInt();
                Map<String, GfxOp> gfxOps = new HashMap<>(size);
                for (int i = 0; i < size; i++) {
                    String key = buf.readUtf(32767);
                    GfxOp gfxOp = GfxOp.readFromBuf(buf);
                    gfxOps.put(key, gfxOp);
                }
                size = buf.readInt();
                List<String> orderedOps = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    orderedOps.add(buf.readUtf(32767));
                }
                return new PacketGraphicsReady(pos, gfxOps, orderedOps);
            }
    );

    public static PacketGraphicsReady create(ProcessorTileEntity processor) {
        return new PacketGraphicsReady(processor.getBlockPos(), processor.getGfxOps(), processor.getOrderedOps());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            BlockEntity te = SafeClientTools.getClientWorld().getBlockEntity(pos);
            if (te instanceof ProcessorTileEntity processor) {
                processor.setClientOrderedGfx(gfxOps, orderedOps);
            }
        });
    }
}
