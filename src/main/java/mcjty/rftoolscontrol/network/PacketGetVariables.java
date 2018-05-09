package mcjty.rftoolscontrol.network;

import mcjty.lib.network.PacketRequestServerList;
import mcjty.lib.network.PacketRequestServerListHandler;
import mcjty.lib.typed.Type;
import mcjty.lib.typed.TypedMap;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.api.parameters.Parameter;
import mcjty.rftoolscontrol.blocks.processor.ProcessorTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nonnull;
import java.util.List;

public class PacketGetVariables extends PacketRequestServerList<Parameter> {

    public PacketGetVariables() {
    }

    public PacketGetVariables(BlockPos pos) {
        super(RFToolsControl.MODID, pos, ProcessorTileEntity.CMD_GETVARS, TypedMap.EMPTY);
    }

    public static class Handler extends PacketRequestServerListHandler<PacketGetVariables, Parameter> {

        public Handler() {
            super(Type.create(Parameter.class));
        }

        @Override
        protected void sendToClient(BlockPos pos, @Nonnull List<Parameter> list, MessageContext messageContext) {
            RFToolsCtrlMessages.INSTANCE.sendTo(new PacketVariablesReady(pos, ProcessorTileEntity.CLIENTCMD_GETVARS, list), messageContext.getServerHandler().player);
        }
    }
}
