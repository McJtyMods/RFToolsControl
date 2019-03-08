package mcjty.rftoolscontrol.network;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.thirteen.Context;
import mcjty.rftoolscontrol.items.ProgramCardItem;
import mcjty.rftoolscontrol.items.craftingcard.CraftingCardItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.function.Supplier;

/**
 * This packet will update the held item NBT from client to server
 */
public class PacketItemNBTToServer implements IMessage {
    private NBTTagCompound tagCompound;

    @Override
    public void fromBytes(ByteBuf buf) {
        tagCompound = NetworkTools.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkTools.writeTag(buf, tagCompound);
    }

    public PacketItemNBTToServer() {
    }

    public PacketItemNBTToServer(ByteBuf buf) {
        fromBytes(buf);
    }

    public PacketItemNBTToServer(NBTTagCompound tagCompound) {
        this.tagCompound = tagCompound;
    }

    public void handle(Supplier<Context> supplier) {
        Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            EntityPlayerMP player = ctx.getSender();
            ItemStack heldItem = player.getHeldItem(EnumHand.MAIN_HAND);
            if (heldItem.isEmpty()) {
                return;
            }
            if (heldItem.getItem() instanceof ProgramCardItem) {
                heldItem.setTagCompound(tagCompound);
            } else if (heldItem.getItem() instanceof CraftingCardItem) {
                heldItem.setTagCompound(tagCompound);
            }
        });
        ctx.setPacketHandled(true);
    }
}