package mcjty.rftoolscontrol.modules.processor.network;


import mcjty.rftoolsbase.api.control.parameters.Parameter;
import mcjty.rftoolsbase.api.control.parameters.ParameterType;
import mcjty.rftoolsbase.api.control.parameters.ParameterValue;
import mcjty.rftoolscontrol.modules.processor.blocks.ProcessorTileEntity;
import mcjty.rftoolscontrol.modules.processor.logic.ParameterTypeTools;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketVariableToServer {
    private final BlockPos pos;
    private final int varIndex;
    private final CompoundTag tagCompound;

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeInt(varIndex);
        buf.writeNbt(tagCompound);
    }

    public PacketVariableToServer(FriendlyByteBuf buf) {
        pos = buf.readBlockPos();
        varIndex = buf.readInt();
        tagCompound = buf.readNbt();
    }

    public PacketVariableToServer(BlockPos pos, int varIndex, CompoundTag tagCompound) {
        this.pos = pos;
        this.varIndex = varIndex;
        this.tagCompound = tagCompound;
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            Player playerEntity = ctx.getSender();
            BlockEntity te = playerEntity.getCommandSenderWorld().getBlockEntity(pos);
            if (te instanceof ProcessorTileEntity processor) {
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