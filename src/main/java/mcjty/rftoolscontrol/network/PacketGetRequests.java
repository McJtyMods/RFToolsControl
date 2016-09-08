package mcjty.rftoolscontrol.network;

import mcjty.lib.network.PacketRequestServerList;
import mcjty.lib.network.PacketRequestServerListHandler;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.blocks.craftingstation.CraftingRequest;
import mcjty.rftoolscontrol.blocks.craftingstation.CraftingStationTileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.List;

public class PacketGetRequests extends PacketRequestServerList<CraftingRequest> {

    public PacketGetRequests() {
    }

    public PacketGetRequests(BlockPos pos) {
        super(RFToolsControl.MODID, pos, CraftingStationTileEntity.CMD_GETREQUESTS);
    }

    public static class Handler extends PacketRequestServerListHandler<PacketGetRequests, CraftingRequest> {

        public Handler() {
        }

        @Override
        protected void sendToClient(BlockPos pos, List<CraftingRequest> list, MessageContext messageContext) {
            RFToolsCtrlMessages.INSTANCE.sendTo(new PacketRequestsReady(pos, CraftingStationTileEntity.CLIENTCMD_GETREQUESTS, list), messageContext.getServerHandler().playerEntity);
        }
    }
}
