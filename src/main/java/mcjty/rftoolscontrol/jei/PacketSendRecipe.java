package mcjty.rftoolscontrol.jei;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.tools.ItemStackTools;
import mcjty.rftoolscontrol.items.ModItems;
import mcjty.rftoolscontrol.items.craftingcard.CraftingCardContainer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;
import java.util.List;

public class PacketSendRecipe implements IMessage {
    private List<ItemStack> stacks;

    @Override
    public void fromBytes(ByteBuf buf) {
        int l = buf.readInt();
        stacks = new ArrayList<>(l);
        for (int i = 0 ; i < l ; i++) {
            if (buf.readBoolean()) {
                stacks.add(NetworkTools.readItemStack(buf));
            } else {
                stacks.add(ItemStackTools.getEmptyStack());
            }
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(stacks.size());
        for (ItemStack stack : stacks) {
            if (ItemStackTools.isValid(stack)) {
                buf.writeBoolean(true);
                NetworkTools.writeItemStack(buf, stack);
            } else {
                buf.writeBoolean(false);
            }
        }
    }

    public PacketSendRecipe() {
    }

    public PacketSendRecipe(List<ItemStack> stacks) {
        this.stacks = stacks;
    }

    public static class Handler implements IMessageHandler<PacketSendRecipe, IMessage> {
        @Override
        public IMessage onMessage(PacketSendRecipe message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketSendRecipe message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            World world = player.getEntityWorld();
            // Handle tablet version
            ItemStack mainhand = player.getHeldItemMainhand();
            if (ItemStackTools.isValid(mainhand) && mainhand.getItem() == ModItems.craftingCardItem) {
                if (player.openContainer instanceof CraftingCardContainer) {
                    CraftingCardContainer craftingCardContainer = (CraftingCardContainer) player.openContainer;
                    craftingCardContainer.setGridContents(player, message.stacks);
                }
            }
        }

    }
}