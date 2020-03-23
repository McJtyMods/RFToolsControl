package mcjty.rftoolscontrol.network;


import mcjty.lib.network.ICommandHandler;
import mcjty.lib.network.TypedMapTools;

import mcjty.lib.typed.Type;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.Logging;
import mcjty.rftoolscontrol.api.parameters.Parameter;
import mcjty.rftoolscontrol.blocks.processor.ProcessorTileEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;


import java.util.List;
import java.util.function.Supplier;

public class PacketGetVariables {

    protected BlockPos pos;
    protected TypedMap params;

    public PacketGetVariables(PacketBuffer buf) {
        pos = buf.readBlockPos();
        params = TypedMapTools.readArguments(buf);
    }

    public PacketGetVariables(BlockPos pos) {
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
            List<Parameter> list = commandHandler.executeWithResultList(ProcessorTileEntity.CMD_GETVARS, params, Type.create(Parameter.class));
            RFToolsCtrlMessages.INSTANCE.sendTo(new PacketVariablesReady(pos, ProcessorTileEntity.CLIENTCMD_GETVARS, list),
                    ctx.getSender().connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
        });
        ctx.setPacketHandled(true);
    }
}
