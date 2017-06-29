package mcjty.rftoolscontrol.network;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.rftoolscontrol.api.parameters.Parameter;
import mcjty.rftoolscontrol.api.parameters.ParameterType;
import mcjty.rftoolscontrol.api.parameters.ParameterValue;
import mcjty.rftoolscontrol.blocks.processor.ProcessorTileEntity;
import mcjty.rftoolscontrol.logic.registry.ParameterTypeTools;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

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

    public PacketVariableToServer(BlockPos pos, int varIndex, NBTTagCompound tagCompound) {
        this.pos = pos;
        this.varIndex = varIndex;
        this.tagCompound = tagCompound;
    }

    public static class Handler implements IMessageHandler<PacketVariableToServer, IMessage> {
        @Override
        public IMessage onMessage(PacketVariableToServer message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketVariableToServer message, MessageContext ctx) {
            EntityPlayerMP playerEntity = ctx.getServerHandler().player;
            TileEntity te = playerEntity.getEntityWorld().getTileEntity(message.pos);
            if (te instanceof ProcessorTileEntity) {
                ProcessorTileEntity processor = (ProcessorTileEntity) te;
                Parameter[] variables = processor.getVariableArray();
                if (message.varIndex < variables.length) {
                    Parameter parameter = variables[message.varIndex];
                    ParameterType type = parameter.getParameterType();
                    ParameterValue value = ParameterTypeTools.readFromNBT(message.tagCompound, type);
                    variables[message.varIndex] = Parameter.builder()
                            .type(type)
                            .value(value)
                            .build();
                    processor.markDirty();
                }
            }
        }
    }
}