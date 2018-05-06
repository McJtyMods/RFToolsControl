package mcjty.rftoolscontrol.network;

import mcjty.lib.network.PacketRequestServerList;
import mcjty.lib.network.PacketRequestServerListHandler;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.blocks.processor.ProcessorTileEntity;
import mcjty.lib.typed.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nonnull;
import java.util.List;

public class PacketGetDebugLog extends PacketRequestServerList<String> {

    public PacketGetDebugLog() {
    }

    public PacketGetDebugLog(BlockPos pos) {
        super(RFToolsControl.MODID, pos, ProcessorTileEntity.CMD_GETDEBUGLOG);
    }

    public static class Handler extends PacketRequestServerListHandler<PacketGetDebugLog, String> {

        public Handler() {
            super(Type.STRING);
        }

        @Override
        protected void sendToClient(BlockPos pos, @Nonnull List<String> list, MessageContext messageContext) {
            RFToolsCtrlMessages.INSTANCE.sendTo(new PacketLogReady(pos, ProcessorTileEntity.CLIENTCMD_GETDEBUGLOG, list), messageContext.getServerHandler().player);
        }
    }
}
