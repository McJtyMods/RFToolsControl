package mcjty.rftoolscontrol.network;

import mcjty.lib.network.PacketRequestServerList;
import mcjty.lib.network.PacketRequestServerListHandler;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.blocks.processor.ProcessorTileEntity;
import mcjty.typed.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nonnull;
import java.util.List;

public class PacketGetLog extends PacketRequestServerList<String> {

    public PacketGetLog() {
    }

    public PacketGetLog(BlockPos pos) {
        super(RFToolsControl.MODID, pos, ProcessorTileEntity.CMD_GETLOG);
    }

    public static class Handler extends PacketRequestServerListHandler<PacketGetLog, String> {

        public Handler() {
            super(Type.STRING);
        }

        @Override
        protected void sendToClient(BlockPos pos, @Nonnull List<String> list, MessageContext messageContext) {
            RFToolsCtrlMessages.INSTANCE.sendTo(new PacketLogReady(pos, ProcessorTileEntity.CLIENTCMD_GETLOG, list), messageContext.getServerHandler().playerEntity);
        }
    }
}
