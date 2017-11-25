package mcjty.rftoolscontrol.items.craftingcard;

import mcjty.lib.network.Argument;
import mcjty.lib.network.PacketUpdateNBTItem;
import mcjty.lib.network.PacketUpdateNBTItemHandler;
import net.minecraft.item.ItemStack;

public class PacketUpdateNBTItemCard extends PacketUpdateNBTItem {

    public PacketUpdateNBTItemCard() {
    }

    public PacketUpdateNBTItemCard(Argument... arguments) {
        super(arguments);
    }

    @Override
    protected boolean isValidItem(ItemStack itemStack) {
        return itemStack.getItem() instanceof CraftingCardItem;
    }

    public static class Handler extends PacketUpdateNBTItemHandler<PacketUpdateNBTItemCard> {

    }
}