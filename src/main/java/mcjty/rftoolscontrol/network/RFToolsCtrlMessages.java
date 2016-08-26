package mcjty.rftoolscontrol.network;

import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

public class RFToolsCtrlMessages {
    public static SimpleNetworkWrapper INSTANCE;

    public static void registerNetworkMessages(SimpleNetworkWrapper net) {
        INSTANCE = net;

        // Server side
//        net.registerMessage(PacketGetDimensionEnergy.Handler.class, PacketGetDimensionEnergy.class, PacketHandler.nextID(), Side.SERVER);

        // Client side
//        net.registerMessage(PacketRegisterDimensions.Handler.class, PacketRegisterDimensions.class, PacketHandler.nextID(), Side.SERVER);
    }
}
