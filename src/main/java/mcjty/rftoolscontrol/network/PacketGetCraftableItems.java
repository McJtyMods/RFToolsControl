package mcjty.rftoolscontrol.network;

import mcjty.lib.network.PacketRequestServerList;
import mcjty.lib.network.PacketRequestServerListHandler;
import mcjty.lib.typed.TypedMap;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.blocks.craftingstation.CraftingStationTileEntity;
import mcjty.lib.typed.Type;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nonnull;
import java.util.List;

public class PacketGetCraftableItems extends PacketRequestServerList<ItemStack> {

    public PacketGetCraftableItems() {
    }

    public PacketGetCraftableItems(BlockPos pos) {
        super(RFToolsControl.MODID, pos, CraftingStationTileEntity.CMD_GETCRAFTABLE, TypedMap.EMPTY);
    }

    public static class Handler extends PacketRequestServerListHandler<PacketGetCraftableItems, ItemStack> {

        public Handler() {
            super(Type.create(ItemStack.class));
        }

        @Override
        protected void sendToClient(BlockPos pos, @Nonnull List<ItemStack> list, MessageContext messageContext) {
            RFToolsCtrlMessages.INSTANCE.sendTo(new PacketCraftableItemsReady(pos, CraftingStationTileEntity.CLIENTCMD_GETCRAFTABLE, list), messageContext.getServerHandler().player);
        }
    }
}
