package mcjty.rftoolscontrol.network;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.blocks.processor.ProcessorTileEntity;
import mcjty.rftoolscontrol.blocks.vectorart.GfxOp;
import mcjty.rftoolscontrol.items.ProgramCardItem;
import mcjty.rftoolscontrol.items.craftingcard.CraftingCardItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PacketGraphicsReady implements IMessage {

    private BlockPos pos;
    private Map<String, GfxOp> gfxOps;
    private List<String> orderedOps;

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = NetworkTools.readPos(buf);
        int size = buf.readInt();
        gfxOps = new HashMap<>(size);
        for (int i = 0 ; i < size ; i++) {
            String key = NetworkTools.readString(buf);
            GfxOp gfxOp = GfxOp.readFromBuf(buf);
            gfxOps.put(key, gfxOp);
        }
        size = buf.readInt();
        orderedOps = new ArrayList<>(size);
        for (int i = 0 ; i < size ; i++) {
            String key = NetworkTools.readString(buf);
            orderedOps.add(key);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkTools.writePos(buf, pos);
        buf.writeInt(gfxOps.size());
        for (Map.Entry<String, GfxOp> entry : gfxOps.entrySet()) {
            NetworkTools.writeString(buf, entry.getKey());
            entry.getValue().writeToBuf(buf);
        }
        buf.writeInt(orderedOps.size());
        for (String op : orderedOps) {
            NetworkTools.writeString(buf, op);
        }
    }

    public PacketGraphicsReady() {
    }

    public PacketGraphicsReady(ProcessorTileEntity processor) {
        pos = processor.getPos();
        gfxOps = processor.getGfxOps();
        orderedOps = processor.getOrderedOps();
    }

    public static class Handler implements IMessageHandler<PacketGraphicsReady, IMessage> {
        @Override
        public IMessage onMessage(PacketGraphicsReady message, MessageContext ctx) {
            RFToolsControl.proxy.addScheduledTaskClient(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketGraphicsReady message, MessageContext ctx) {
            TileEntity te = RFToolsControl.proxy.getClientWorld().getTileEntity(message.pos);
            if (te instanceof ProcessorTileEntity) {
                ProcessorTileEntity processor = (ProcessorTileEntity) te;
                processor.setClientOrderedGfx(message.gfxOps, message.orderedOps);
            }
        }
    }
}