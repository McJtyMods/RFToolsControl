package mcjty.rftoolscontrol.network;

import mcjty.lib.network.PacketRequestServerList;
import mcjty.lib.network.PacketRequestServerListHandler;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.api.parameters.Parameter;
import mcjty.rftoolscontrol.blocks.processor.ProcessorTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.List;

public class PacketGetFluids extends PacketRequestServerList<PacketGetFluids.FluidEntry> {

    public PacketGetFluids() {
    }

    public PacketGetFluids(BlockPos pos) {
        super(RFToolsControl.MODID, pos, ProcessorTileEntity.CMD_GETFLUIDS);
    }

    public static class Handler extends PacketRequestServerListHandler<PacketGetFluids, Parameter> {

        public Handler() {
        }

        @Override
        protected void sendToClient(BlockPos pos, List<Parameter> list, MessageContext messageContext) {
            RFToolsCtrlMessages.INSTANCE.sendTo(new PacketVariablesReady(pos, ProcessorTileEntity.CLIENTCMD_GETFLUIDS, list), messageContext.getServerHandler().playerEntity);
        }
    }

    public static class FluidEntry {
        private final FluidStack fluidStack;
        private final boolean allocated;

        public FluidEntry(FluidStack fluidStack, boolean allocated) {
            this.fluidStack = fluidStack;
            this.allocated = allocated;
        }

        public FluidStack getFluidStack() {
            return fluidStack;
        }

        public boolean isAllocated() {
            return allocated;
        }
    }
}
