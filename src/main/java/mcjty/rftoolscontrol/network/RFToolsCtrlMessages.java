package mcjty.rftoolscontrol.network;

import mcjty.lib.network.PacketHandler;
import mcjty.rftoolscontrol.jei.PacketSendRecipe;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class RFToolsCtrlMessages {
    public static SimpleNetworkWrapper INSTANCE;

    public static void registerNetworkMessages(SimpleNetworkWrapper net) {
        INSTANCE = net;

        // Server side
        net.registerMessage(PacketGetLog.Handler.class, PacketGetLog.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketGetDebugLog.Handler.class, PacketGetDebugLog.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketGetVariables.Handler.class, PacketGetVariables.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketGetCraftableItems.Handler.class, PacketGetCraftableItems.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketGetRequests.Handler.class, PacketGetRequests.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketSendRecipe.Handler.class, PacketSendRecipe.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketItemNBTToServer.Handler.class, PacketItemNBTToServer.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketVariableToServer.Handler.class, PacketVariableToServer.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketTestRecipe.Handler.class, PacketTestRecipe.class, PacketHandler.nextID(), Side.SERVER);

        // Client side
        net.registerMessage(PacketLogReady.Handler.class, PacketLogReady.class, PacketHandler.nextID(), Side.CLIENT);
        net.registerMessage(PacketVariablesReady.Handler.class, PacketVariablesReady.class, PacketHandler.nextID(), Side.CLIENT);
        net.registerMessage(PacketCraftableItemsReady.Handler.class, PacketCraftableItemsReady.class, PacketHandler.nextID(), Side.CLIENT);
        net.registerMessage(PacketRequestsReady.Handler.class, PacketRequestsReady.class, PacketHandler.nextID(), Side.CLIENT);
    }
}
