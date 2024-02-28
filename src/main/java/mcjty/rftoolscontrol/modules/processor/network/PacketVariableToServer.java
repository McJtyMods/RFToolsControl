package mcjty.rftoolscontrol.modules.processor.network;


import mcjty.lib.network.CustomPacketPayload;
import mcjty.lib.network.PlayPayloadContext;
import mcjty.rftoolsbase.api.control.parameters.Parameter;
import mcjty.rftoolsbase.api.control.parameters.ParameterType;
import mcjty.rftoolsbase.api.control.parameters.ParameterValue;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.modules.processor.blocks.ProcessorTileEntity;
import mcjty.rftoolscontrol.modules.processor.logic.ParameterTypeTools;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.network.NetworkEvent;

import java.util.function.Supplier;

public record PacketVariableToServer(BlockPos pos, int varIndex, CompoundTag tagCompound) implements CustomPacketPayload {

    public static final ResourceLocation ID = new ResourceLocation(RFToolsControl.MODID, "variable_to_server");

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeInt(varIndex);
        buf.writeNbt(tagCompound);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static PacketVariableToServer create(FriendlyByteBuf buf) {
        return new PacketVariableToServer(buf.readBlockPos(), buf.readInt(), buf.readNbt());
    }

    public static PacketVariableToServer create(BlockPos blockPos, int varIdx, CompoundTag tag) {
        return new PacketVariableToServer(blockPos, varIdx, tag);
    }

    public void handle(PlayPayloadContext ctx) {
        ctx.workHandler().submitAsync(() -> {
            ctx.player().ifPresent(playerEntity -> {
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
        });
    }
}