package mcjty.rftoolscontrol.setup;

import mcjty.lib.network.PacketHandler;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.modules.processor.network.*;
import mcjty.rftoolscontrol.modules.programmer.network.PacketUpdateNBTItemInventoryProgrammer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class RFToolsCtrlMessages {
    public static SimpleChannel INSTANCE;

    private static int packetId = 0;
    private static int id() {
        return packetId++;
    }

    public static void registerMessages(String name) {
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(RFToolsControl.MODID, name))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE = net;

        // Server side
        net.registerMessage(id(), PacketGetLog.class, PacketGetLog::toBytes, PacketGetLog::new, PacketGetLog::handle);
        net.registerMessage(id(), PacketGetVariables.class, PacketGetVariables::toBytes, PacketGetVariables::new, PacketGetVariables::handle);
        net.registerMessage(id(), PacketGetFluids.class, PacketGetFluids::toBytes, PacketGetFluids::new, PacketGetFluids::handle);
        net.registerMessage(id(), PacketVariableToServer.class, PacketVariableToServer::toBytes, PacketVariableToServer::new, PacketVariableToServer::handle);
        net.registerMessage(id(), PacketUpdateNBTItemInventoryProgrammer.class, PacketUpdateNBTItemInventoryProgrammer::toBytes, PacketUpdateNBTItemInventoryProgrammer::new, PacketUpdateNBTItemInventoryProgrammer::handle);

        // Client side
        net.registerMessage(id(), PacketLogReady.class, PacketLogReady::toBytes, PacketLogReady::new, PacketLogReady::handle);
        net.registerMessage(id(), PacketVariablesReady.class, PacketVariablesReady::toBytes, PacketVariablesReady::new, PacketVariablesReady::handle);
        net.registerMessage(id(), PacketFluidsReady.class, PacketFluidsReady::toBytes, PacketFluidsReady::new, PacketFluidsReady::handle);
        net.registerMessage(id(), PacketGraphicsReady.class, PacketGraphicsReady::toBytes, PacketGraphicsReady::new, PacketGraphicsReady::handle);

        PacketHandler.registerStandardMessages(id(), net);
    }
}
