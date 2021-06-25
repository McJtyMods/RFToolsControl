package mcjty.rftoolscontrol.modules.processor.network;


import mcjty.rftoolsbase.api.control.parameters.ParameterType;
import mcjty.rftoolsbase.api.control.parameters.ParameterValue;
import mcjty.rftoolscontrol.modules.processor.blocks.ProcessorTileEntity;
import mcjty.rftoolscontrol.modules.processor.logic.Parameter;
import mcjty.rftoolscontrol.modules.processor.logic.ParameterTypeTools;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketVariableToServer {
    private BlockPos pos;
    private int varIndex;
    private CompoundNBT tagCompound;

    public void toBytes(PacketBuffer buf) {
        buf.writeBlockPos(pos);
        buf.writeInt(varIndex);
        buf.writeNbt(tagCompound);
    }

    public PacketVariableToServer() {
    }

    public PacketVariableToServer(PacketBuffer buf) {
        pos = buf.readBlockPos();
        varIndex = buf.readInt();
        tagCompound = buf.readNbt();
    }

    public PacketVariableToServer(BlockPos pos, int varIndex, CompoundNBT tagCompound) {
        this.pos = pos;
        this.varIndex = varIndex;
        this.tagCompound = tagCompound;
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            PlayerEntity playerEntity = ctx.getSender();
            TileEntity te = playerEntity.getCommandSenderWorld().getBlockEntity(pos);
            if (te instanceof ProcessorTileEntity) {
                ProcessorTileEntity processor = (ProcessorTileEntity) te;
                Parameter[] variables = processor.getVariableArray();
                if (varIndex < variables.length) {
                    Parameter parameter = variables[varIndex];
                    ParameterType type = parameter.getParameterType();
                    ParameterValue value = ParameterTypeTools.readFromNBT(tagCompound, type);
                    // Here we don't want to trigger the watch
                    variables[varIndex] = Parameter.builder()
                            .type(type)
                            .value(value)
                            .build();
                    processor.setChanged();
                }
            }
        });
        ctx.setPacketHandled(true);
    }
}