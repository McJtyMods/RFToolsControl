package mcjty.rftoolscontrol.compat.jei;

import mcjty.lib.network.NetworkTools;
import mcjty.rftoolsbase.modules.crafting.CraftingSetup;
import mcjty.rftoolsbase.modules.crafting.items.CraftingCardContainer;
import mcjty.rftoolscontrol.items.ModItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public class PacketSendRecipe {
    private List<ItemStack> stacks;

    public void toBytes(PacketBuffer buf) {
        NetworkTools.writeItemStackList(buf, stacks);
    }

    public PacketSendRecipe(PacketBuffer buf) {
        stacks = NetworkTools.readItemStackList(buf);
    }

    public PacketSendRecipe(List<ItemStack> stacks) {
        this.stacks = stacks;
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            PlayerEntity player = ctx.getSender();
            World world = player.getEntityWorld();
            // Handle tablet version
            ItemStack mainhand = player.getHeldItemMainhand();
            if (!mainhand.isEmpty() && mainhand.getItem() == CraftingSetup.CRAFTING_CARD.get()) {   // @todo 1.15 object holder
                if (player.openContainer instanceof CraftingCardContainer) {
                    CraftingCardContainer craftingCardContainer = (CraftingCardContainer) player.openContainer;
                    craftingCardContainer.setGridContents(player, stacks);
                }
            }
        });
        ctx.setPacketHandled(true);
    }
}