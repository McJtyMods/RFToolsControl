package mcjty.rftoolscontrol.modules.processor.network;


import mcjty.lib.McJtyLib;
import mcjty.rftoolscontrol.modules.processor.blocks.ProcessorTileEntity;
import mcjty.rftoolscontrol.modules.processor.vectorart.GfxOp;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class PacketGraphicsReady {

    private BlockPos pos;
    private Map<String, GfxOp> gfxOps;
    private List<String> orderedOps;

    public void toBytes(PacketBuffer buf) {
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

    public PacketGraphicsReady(PacketBuffer buf) {
        pos = buf.readBlockPos();
        int size = buf.readInt();
        gfxOps = new HashMap<>(size);
        for (int i = 0 ; i < size ; i++) {
            String key = buf.readUtf(32767);
            GfxOp gfxOp = GfxOp.readFromBuf(buf);
            gfxOps.put(key, gfxOp);
        }
        size = buf.readInt();
        orderedOps = new ArrayList<>(size);
        for (int i = 0 ; i < size ; i++) {
            String key = buf.readUtf(32767);
            orderedOps.add(key);
        }
    }

    public PacketGraphicsReady(ProcessorTileEntity processor) {
        pos = processor.getBlockPos();
        gfxOps = processor.getGfxOps();
        orderedOps = processor.getOrderedOps();
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            TileEntity te = McJtyLib.proxy.getClientWorld().getBlockEntity(pos);
            if (te instanceof ProcessorTileEntity) {
                ProcessorTileEntity processor = (ProcessorTileEntity) te;
                processor.setClientOrderedGfx(gfxOps, orderedOps);
            }
        });
        ctx.setPacketHandled(true);
    }
}