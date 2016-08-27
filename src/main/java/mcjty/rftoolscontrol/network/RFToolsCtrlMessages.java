package mcjty.rftoolscontrol.network;

import mcjty.lib.network.PacketHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class RFToolsCtrlMessages {
    public static SimpleNetworkWrapper INSTANCE;

    public static void registerNetworkMessages(SimpleNetworkWrapper net) {
        INSTANCE = net;

        // Server side
//        net.registerMessage(PacketUpdateNBTItemInventory.Handler.class, PacketUpdateNBTItemInventory.class, PacketHandler.nextID(), Side.SERVER);

        // Client side
//        net.registerMessage(PacketRegisterDimensions.Handler.class, PacketRegisterDimensions.class, PacketHandler.nextID(), Side.SERVER);
    }
}
