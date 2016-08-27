package mcjty.rftoolscontrol.network;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.io.IOException;

/**
 * This is a packet that can be used to update the NBT of an item in an inventory.
 */
public class PacketUpdateNBTItemInventory implements IMessage {
    private BlockPos pos;
    private int slotIndex;
    private NBTTagCompound tagCompound;

    // @todo temporary here
    public static NBTTagCompound readTag(ByteBuf dataIn) {
        PacketBuffer buf = new PacketBuffer(dataIn);
        try {
            return buf.readNBTTagCompoundFromBuffer();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void writeTag(ByteBuf dataOut, NBTTagCompound tag) {
        PacketBuffer buf = new PacketBuffer(dataOut);
        try {
            buf.writeNBTTagCompoundToBuffer(tag);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = NetworkTools.readPos(buf);
        slotIndex = buf.readInt();
        tagCompound = readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkTools.writePos(buf, pos);
        buf.writeInt(slotIndex);
        writeTag(buf, tagCompound);
    }

    public PacketUpdateNBTItemInventory() {
    }

    public PacketUpdateNBTItemInventory(BlockPos pos, int slotIndex, NBTTagCompound tagCompound) {
        this.pos = pos;
        this.slotIndex = slotIndex;
        this.tagCompound = tagCompound;
    }

    public static class Handler implements IMessageHandler<PacketUpdateNBTItemInventory, IMessage> {
        @Override
        public IMessage onMessage(PacketUpdateNBTItemInventory message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketUpdateNBTItemInventory message, MessageContext ctx) {
            World world = ctx.getServerHandler().playerEntity.worldObj;
            TileEntity te = world.getTileEntity(message.pos);
            if (te instanceof IInventory) {
                IInventory inv = (IInventory) te;
                ItemStack stack = inv.getStackInSlot(message.slotIndex);
                if (stack != null) {
                    stack.setTagCompound(message.tagCompound);
                    System.out.println("message.tagCompound = " + message.tagCompound);
                }
            }
        }

    }

}
