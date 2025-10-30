package mcjty.rftoolscontrol.modules.processor.network;


import mcjty.rftoolsbase.api.control.parameters.Parameter;
import mcjty.rftoolsbase.api.control.parameters.ParameterType;
import mcjty.rftoolsbase.api.control.parameters.ParameterValue;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.modules.processor.blocks.ProcessorTileEntity;
import mcjty.rftoolscontrol.modules.processor.logic.ParameterTypeTools;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record PacketVariableToServer(BlockPos pos, int varIndex, CompoundTag tagCompound) implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(RFToolsControl.MODID, "variable_to_server");
    public static final CustomPacketPayload.Type<PacketVariableToServer> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketVariableToServer> CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeBlockPos(packet.pos);
                buf.writeInt(packet.varIndex);
                buf.writeNbt(packet.tagCompound);
            },
            buf -> new PacketVariableToServer(buf.readBlockPos(), buf.readInt(), buf.readNbt())
    );

    public static PacketVariableToServer create(BlockPos blockPos, int varIdx, CompoundTag tag) {
        return new PacketVariableToServer(blockPos, varIdx, tag);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            BlockEntity te = ctx.player().getCommandSenderWorld().getBlockEntity(pos);
            if (te instanceof ProcessorTileEntity processor) {
                List<Parameter> variables = processor.getVariableArray();
                if (varIndex < variables.size()) {
                    Parameter parameter = variables.get(varIndex);
                    ParameterType type = parameter.getParameterType();
                    ParameterValue value = ParameterTypeTools.readFromNBT(tagCompound, type, ctx.player().registryAccess());
                    // Here we don't want to trigger the watch
                    variables.set(varIndex, Parameter.builder()
                            .type(type)
                            .value(value)
                            .build());
                    processor.setChanged();
                }
            }
        });
    }
}
