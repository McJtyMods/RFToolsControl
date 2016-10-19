package mcjty.rftoolscontrol.network;

import mcjty.lib.network.PacketRequestServerList;
import mcjty.lib.network.PacketRequestServerListHandler;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.blocks.multitank.MultiTankTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.List;

public class PacketGetTankFluids extends PacketRequestServerList<FluidStack> {

    public PacketGetTankFluids() {
    }

    public PacketGetTankFluids(BlockPos pos) {
        super(RFToolsControl.MODID, pos, MultiTankTileEntity.CMD_GETFLUIDS);
    }

    public static class Handler extends PacketRequestServerListHandler<PacketGetTankFluids, FluidStack> {

        public Handler() {
        }

        @Override
        protected void sendToClient(BlockPos pos, List<FluidStack> list, MessageContext messageContext) {
            RFToolsCtrlMessages.INSTANCE.sendTo(new PacketTankFluidsReady(pos, MultiTankTileEntity.CLIENTCMD_GETFLUIDS, list), messageContext.getServerHandler().playerEntity);
        }
    }
}
