package mcjty.rftoolscontrol.network;

import mcjty.lib.network.PacketHandler;
import mcjty.rftoolscontrol.blocks.programmer.PacketUpdateNBTItemInventoryProgrammer;
import mcjty.rftoolscontrol.items.craftingcard.PacketUpdateNBTItemCard;
import mcjty.rftoolscontrol.jei.PacketSendRecipe;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class RFToolsCtrlMessages {
    public static SimpleNetworkWrapper INSTANCE;

    public static void registerNetworkMessages(SimpleNetworkWrapper net) {
        INSTANCE = net;

        // Server side
        net.registerMessage(PacketGetLog.Handler.class, PacketGetLog.class, PacketHandler.nextPacketID(), Side.SERVER);
        net.registerMessage(PacketGetDebugLog.Handler.class, PacketGetDebugLog.class, PacketHandler.nextPacketID(), Side.SERVER);
        net.registerMessage(PacketGetVariables.Handler.class, PacketGetVariables.class, PacketHandler.nextPacketID(), Side.SERVER);
        net.registerMessage(PacketGetFluids.Handler.class, PacketGetFluids.class, PacketHandler.nextPacketID(), Side.SERVER);
        net.registerMessage(PacketGetTankFluids.Handler.class, PacketGetTankFluids.class, PacketHandler.nextPacketID(), Side.SERVER);
        net.registerMessage(PacketGetCraftableItems.Handler.class, PacketGetCraftableItems.class, PacketHandler.nextPacketID(), Side.SERVER);
        net.registerMessage(PacketGetRequests.Handler.class, PacketGetRequests.class, PacketHandler.nextPacketID(), Side.SERVER);
        net.registerMessage(PacketSendRecipe.Handler.class, PacketSendRecipe.class, PacketHandler.nextPacketID(), Side.SERVER);
        net.registerMessage(PacketItemNBTToServer.Handler.class, PacketItemNBTToServer.class, PacketHandler.nextPacketID(), Side.SERVER);
        net.registerMessage(PacketVariableToServer.Handler.class, PacketVariableToServer.class, PacketHandler.nextPacketID(), Side.SERVER);
        net.registerMessage(PacketUpdateNBTItemInventoryProgrammer.Handler.class, PacketUpdateNBTItemInventoryProgrammer.class, PacketHandler.nextPacketID(), Side.SERVER);
        net.registerMessage(PacketUpdateNBTItemCard.Handler.class, PacketUpdateNBTItemCard.class, PacketHandler.nextPacketID(), Side.SERVER);

        // Client side
        net.registerMessage(PacketLogReady.Handler.class, PacketLogReady.class, PacketHandler.nextPacketID(), Side.CLIENT);
        net.registerMessage(PacketVariablesReady.Handler.class, PacketVariablesReady.class, PacketHandler.nextPacketID(), Side.CLIENT);
        net.registerMessage(PacketFluidsReady.Handler.class, PacketFluidsReady.class, PacketHandler.nextPacketID(), Side.CLIENT);
        net.registerMessage(PacketTankFluidsReady.Handler.class, PacketTankFluidsReady.class, PacketHandler.nextPacketID(), Side.CLIENT);
        net.registerMessage(PacketCraftableItemsReady.Handler.class, PacketCraftableItemsReady.class, PacketHandler.nextPacketID(), Side.CLIENT);
        net.registerMessage(PacketRequestsReady.Handler.class, PacketRequestsReady.class, PacketHandler.nextPacketID(), Side.CLIENT);
        net.registerMessage(PacketGraphicsReady.Handler.class, PacketGraphicsReady.class, PacketHandler.nextPacketID(), Side.CLIENT);
    }
}
