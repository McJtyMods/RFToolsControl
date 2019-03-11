package mcjty.rftoolscontrol.compat.jei;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.thirteen.Context;
import mcjty.rftoolscontrol.items.ModItems;
import mcjty.rftoolscontrol.items.craftingcard.CraftingCardContainer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.List;
import java.util.function.Supplier;

public class PacketSendRecipe implements IMessage {
    private List<ItemStack> stacks;

    @Override
    public void fromBytes(ByteBuf buf) {
        stacks = NetworkTools.readItemStackList(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkTools.writeItemStackList(buf, stacks);
    }

    public PacketSendRecipe() {
    }

    public PacketSendRecipe(ByteBuf buf) {
        fromBytes(buf);
    }

    public PacketSendRecipe(List<ItemStack> stacks) {
        this.stacks = stacks;
    }

    public void handle(Supplier<Context> supplier) {
        Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            EntityPlayerMP player = ctx.getSender();
            World world = player.getEntityWorld();
            // Handle tablet version
            ItemStack mainhand = player.getHeldItemMainhand();
            if (!mainhand.isEmpty() && mainhand.getItem() == ModItems.craftingCardItem) {
                if (player.openContainer instanceof CraftingCardContainer) {
                    CraftingCardContainer craftingCardContainer = (CraftingCardContainer) player.openContainer;
                    craftingCardContainer.setGridContents(player, stacks);
                }
            }
        });
        ctx.setPacketHandled(true);
    }
}