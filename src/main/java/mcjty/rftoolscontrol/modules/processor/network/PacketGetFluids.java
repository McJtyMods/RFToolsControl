package mcjty.rftoolscontrol.modules.processor.network;


import mcjty.lib.network.ICommandHandler;
import mcjty.lib.network.TypedMapTools;
import mcjty.lib.typed.Type;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.Logging;
import mcjty.rftoolscontrol.modules.processor.blocks.ProcessorTileEntity;
import mcjty.rftoolscontrol.setup.RFToolsCtrlMessages;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public class PacketGetFluids {

    protected BlockPos pos;
    protected TypedMap params;

    public PacketGetFluids(PacketBuffer buf) {
        pos = buf.readBlockPos();
        params = TypedMapTools.readArguments(buf);
    }

    public PacketGetFluids(BlockPos pos) {
        this.pos = pos;
        this.params = TypedMap.EMPTY;
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeBlockPos(pos);
        TypedMapTools.writeArguments(buf, params);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            TileEntity te = ctx.getSender().getEntityWorld().getTileEntity(pos);
            if(!(te instanceof ICommandHandler)) {
                Logging.log("TileEntity is not a CommandHandler!");
                return;
            }
            ICommandHandler commandHandler = (ICommandHandler) te;
            List<FluidEntry> list = commandHandler.executeWithResultList(ProcessorTileEntity.CMD_GETFLUIDS, params, Type.create(FluidEntry.class));
            RFToolsCtrlMessages.INSTANCE.sendTo(new PacketFluidsReady(pos, ProcessorTileEntity.CLIENTCMD_GETFLUIDS, list), ctx.getSender().connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
        });
        ctx.setPacketHandled(true);
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
