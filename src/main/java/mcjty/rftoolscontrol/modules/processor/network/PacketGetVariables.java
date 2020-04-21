package mcjty.rftoolscontrol.modules.processor.network;


import mcjty.lib.network.ICommandHandler;
import mcjty.lib.network.TypedMapTools;
import mcjty.lib.typed.Type;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.Logging;
import mcjty.lib.varia.WorldTools;
import mcjty.rftoolscontrol.modules.processor.blocks.ProcessorTileEntity;
import mcjty.rftoolscontrol.modules.processor.logic.Parameter;
import mcjty.rftoolscontrol.setup.RFToolsCtrlMessages;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public class PacketGetVariables {

    private BlockPos pos;
    private DimensionType type;
    private TypedMap params;

    public PacketGetVariables(PacketBuffer buf) {
        pos = buf.readBlockPos();
        type = DimensionType.getById(buf.readInt());
        params = TypedMapTools.readArguments(buf);
    }

    public PacketGetVariables(BlockPos pos, DimensionType type) {
        this.pos = pos;
        this.type = type;
        this.params = TypedMap.EMPTY;
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeBlockPos(pos);
        buf.writeInt(type.getId());
        TypedMapTools.writeArguments(buf, params);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            TileEntity te = WorldTools.getWorld(ctx.getSender().getEntityWorld(), type).getTileEntity(pos);
            if(!(te instanceof ICommandHandler)) {
                Logging.log("TileEntity is not a CommandHandler!");
                return;
            }
            ICommandHandler commandHandler = (ICommandHandler) te;
            List<Parameter> list = commandHandler.executeWithResultList(ProcessorTileEntity.CMD_GETVARS, params, Type.create(Parameter.class));
            RFToolsCtrlMessages.INSTANCE.sendTo(new PacketVariablesReady(((ProcessorTileEntity)te).isDummy() ? null : pos, ProcessorTileEntity.CLIENTCMD_GETVARS, list),
                    ctx.getSender().connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
        });
        ctx.setPacketHandled(true);
    }
}
