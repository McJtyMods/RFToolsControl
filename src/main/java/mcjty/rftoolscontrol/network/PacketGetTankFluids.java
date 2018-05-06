package mcjty.rftoolscontrol.network;

import mcjty.lib.network.PacketRequestServerList;
import mcjty.lib.network.PacketRequestServerListHandler;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.blocks.multitank.MultiTankTileEntity;
import mcjty.lib.typed.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nonnull;
import java.util.List;

public class PacketGetTankFluids extends PacketRequestServerList<FluidStack> {

    public PacketGetTankFluids() {
    }

    public PacketGetTankFluids(BlockPos pos) {
        super(RFToolsControl.MODID, pos, MultiTankTileEntity.CMD_GETFLUIDS);
    }

    public static class Handler extends PacketRequestServerListHandler<PacketGetTankFluids, FluidStack> {

        public Handler() {
            super(Type.create(FluidStack.class));
        }

        @Override
        protected void sendToClient(BlockPos pos, @Nonnull List<FluidStack> list, MessageContext messageContext) {
            RFToolsCtrlMessages.INSTANCE.sendTo(new PacketTankFluidsReady(pos, MultiTankTileEntity.CLIENTCMD_GETFLUIDS, list), messageContext.getServerHandler().player);
        }
    }
}
