package mcjty.rftoolscontrol.network;

import mcjty.lib.network.PacketHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class RFToolsCtrlMessages {
    public static SimpleNetworkWrapper INSTANCE;

    public static void registerNetworkMessages(SimpleNetworkWrapper net) {
        INSTANCE = net;

        // Server side
        net.registerMessage(PacketGetLog.Handler.class, PacketGetLog.class, PacketHandler.nextID(), Side.SERVER);

        // Client side
        net.registerMessage(PacketLogReady.Handler.class, PacketLogReady.class, PacketHandler.nextID(), Side.SERVER);
    }
}
