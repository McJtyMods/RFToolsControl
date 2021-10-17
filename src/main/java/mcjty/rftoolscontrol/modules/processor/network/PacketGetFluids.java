package mcjty.rftoolscontrol.modules.processor.network;


import mcjty.lib.network.ICommandHandler;
import mcjty.lib.network.TypedMapTools;
import mcjty.lib.typed.Type;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.Logging;
import mcjty.lib.varia.LevelTools;
import mcjty.rftoolscontrol.modules.processor.blocks.ProcessorTileEntity;
import mcjty.rftoolscontrol.setup.RFToolsCtrlMessages;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public class PacketGetFluids {

    private BlockPos pos;
    private RegistryKey<World> type;
    private TypedMap params;
    private boolean fromTablet;

    public PacketGetFluids(PacketBuffer buf) {
        pos = buf.readBlockPos();
        type = LevelTools.getId(buf.readResourceLocation());
        params = TypedMapTools.readArguments(buf);
        fromTablet = buf.readBoolean();
    }

    public PacketGetFluids(BlockPos pos, RegistryKey<World> type, boolean fromTablet) {
        this.pos = pos;
        this.type = type;
        this.params = TypedMap.EMPTY;
        this.fromTablet = fromTablet;
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeBlockPos(pos);
        buf.writeResourceLocation(type.location());
        TypedMapTools.writeArguments(buf, params);
        buf.writeBoolean(fromTablet);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerWorld world = LevelTools.getLevel(ctx.getSender().getCommandSenderWorld(), type);
            if (world.hasChunkAt(pos)) {
                TileEntity te = world.getBlockEntity(pos);
                if (!(te instanceof ICommandHandler)) {
                    Logging.log("TileEntity is not a CommandHandler!");
                    return;
                }
                ICommandHandler commandHandler = (ICommandHandler) te;
                List<FluidEntry> list = commandHandler.executeWithResultList(ProcessorTileEntity.CMD_GETFLUIDS, params, Type.create(FluidEntry.class));
                RFToolsCtrlMessages.INSTANCE.sendTo(new PacketFluidsReady(fromTablet ? null : pos, ProcessorTileEntity.CLIENTCMD_GETFLUIDS, list), ctx.getSender().connection.connection, NetworkDirection.PLAY_TO_CLIENT);
            }
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
