package mcjty.rftoolscontrol.network;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.thirteen.Context;
import mcjty.rftoolscontrol.api.parameters.Parameter;
import mcjty.rftoolscontrol.api.parameters.ParameterType;
import mcjty.rftoolscontrol.api.parameters.ParameterValue;
import mcjty.rftoolscontrol.blocks.processor.ProcessorTileEntity;
import mcjty.rftoolscontrol.logic.ParameterTypeTools;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.function.Supplier;

public class PacketVariableToServer implements IMessage {
    private BlockPos pos;
    private int varIndex;
    private NBTTagCompound tagCompound;

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = NetworkTools.readPos(buf);
        varIndex = buf.readInt();
        tagCompound = NetworkTools.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkTools.writePos(buf, pos);
        buf.writeInt(varIndex);
        NetworkTools.writeTag(buf, tagCompound);
    }

    public PacketVariableToServer() {
    }

    public PacketVariableToServer(ByteBuf buf) {
        fromBytes(buf);
    }

    public PacketVariableToServer(BlockPos pos, int varIndex, NBTTagCompound tagCompound) {
        this.pos = pos;
        this.varIndex = varIndex;
        this.tagCompound = tagCompound;
    }

    public void handle(Supplier<Context> supplier) {
        Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            EntityPlayerMP playerEntity = ctx.getSender();
            TileEntity te = playerEntity.getEntityWorld().getTileEntity(pos);
            if (te instanceof ProcessorTileEntity) {
                ProcessorTileEntity processor = (ProcessorTileEntity) te;
                Parameter[] variables = processor.getVariableArray();
                if (varIndex < variables.length) {
                    Parameter parameter = variables[varIndex];
                    ParameterType type = parameter.getParameterType();
                    ParameterValue value = ParameterTypeTools.readFromNBT(tagCompound, type);
                    variables[varIndex] = Parameter.builder()
                            .type(type)
                            .value(value)
                            .build();
                    processor.markDirty();
                }
            }
        });
        ctx.setPacketHandled(true);
    }
}