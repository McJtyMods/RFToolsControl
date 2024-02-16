package mcjty.rftoolscontrol.modules.processor.network;


import mcjty.lib.network.CustomPacketPayload;
import mcjty.lib.network.PlayPayloadContext;
import mcjty.lib.varia.SafeClientTools;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.modules.processor.blocks.ProcessorTileEntity;
import mcjty.rftoolscontrol.modules.processor.vectorart.GfxOp;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record PacketGraphicsReady(BlockPos pos, Map<String, GfxOp> gfxOps, List<String> orderedOps) implements CustomPacketPayload {

    public static final ResourceLocation ID = new ResourceLocation(RFToolsControl.MODID, "graphics_ready");

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeInt(gfxOps.size());
        for (Map.Entry<String, GfxOp> entry : gfxOps.entrySet()) {
            buf.writeUtf(entry.getKey());
            entry.getValue().writeToBuf(buf);
        }
        buf.writeInt(orderedOps.size());
        for (String op : orderedOps) {
            buf.writeUtf(op);
        }
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static PacketGraphicsReady create(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        int size = buf.readInt();
        Map<String, GfxOp> gfxOps = new HashMap<>(size);
        for (int i = 0 ; i < size ; i++) {
            String key = buf.readUtf(32767);
            GfxOp gfxOp = GfxOp.readFromBuf(buf);
            gfxOps.put(key, gfxOp);
        }
        size = buf.readInt();
        List<String> orderedOps = new ArrayList<>(size);
        for (int i = 0 ; i < size ; i++) {
            String key = buf.readUtf(32767);
            orderedOps.add(key);
        }
        return new PacketGraphicsReady(pos, gfxOps, orderedOps);
    }

    public static PacketGraphicsReady create(ProcessorTileEntity processor) {
        return new PacketGraphicsReady(processor.getBlockPos(), processor.getGfxOps(), processor.getOrderedOps());
    }

    public void handle(PlayPayloadContext ctx) {
        ctx.workHandler().submitAsync(() -> {
            BlockEntity te = SafeClientTools.getClientWorld().getBlockEntity(pos);
            if (te instanceof ProcessorTileEntity processor) {
                processor.setClientOrderedGfx(gfxOps, orderedOps);
            }
        });
    }
}