package mcjty.rftoolscontrol.network;

import mcjty.lib.network.PacketRequestServerList;
import mcjty.lib.network.PacketRequestServerListHandler;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.blocks.craftingstation.CraftingStationTileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.List;

public class PacketGetCraftableItems extends PacketRequestServerList<ItemStack> {

    public PacketGetCraftableItems() {
    }

    public PacketGetCraftableItems(BlockPos pos) {
        super(RFToolsControl.MODID, pos, CraftingStationTileEntity.CMD_GETCRAFTABLE);
    }

    public static class Handler extends PacketRequestServerListHandler<PacketGetCraftableItems, ItemStack> {

        public Handler() {
        }

        @Override
        protected void sendToClient(BlockPos pos, List<ItemStack> list, MessageContext messageContext) {
            RFToolsCtrlMessages.INSTANCE.sendTo(new PacketCraftableItemsReady(pos, CraftingStationTileEntity.CLIENTCMD_GETCRAFTABLE, list), messageContext.getServerHandler().playerEntity);
        }
    }
}
