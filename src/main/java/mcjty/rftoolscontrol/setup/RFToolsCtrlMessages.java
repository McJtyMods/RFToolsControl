package mcjty.rftoolscontrol.setup;

import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.modules.processor.network.*;
import mcjty.rftoolscontrol.modules.programmer.network.PacketUpdateNBTItemInventoryProgrammer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import static mcjty.lib.network.PlayPayloadContext.wrap;

public class RFToolsCtrlMessages {
    private static SimpleChannel INSTANCE;

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
        net.registerMessage(id(), PacketGetLog.class, PacketGetLog::write, PacketGetLog::create, wrap(PacketGetLog::handle));
        net.registerMessage(id(), PacketGetVariables.class, PacketGetVariables::write, PacketGetVariables::create, wrap(PacketGetVariables::handle));
        net.registerMessage(id(), PacketGetFluids.class, PacketGetFluids::write, PacketGetFluids::create, wrap(PacketGetFluids::handle));
        net.registerMessage(id(), PacketVariableToServer.class, PacketVariableToServer::write, PacketVariableToServer::create, wrap(PacketVariableToServer::handle));
        net.registerMessage(id(), PacketUpdateNBTItemInventoryProgrammer.class, PacketUpdateNBTItemInventoryProgrammer::write, PacketUpdateNBTItemInventoryProgrammer::create, wrap(PacketUpdateNBTItemInventoryProgrammer::handle));

        // Client side
        net.registerMessage(id(), PacketLogReady.class, PacketLogReady::write, PacketLogReady::create, wrap(PacketLogReady::handle));
        net.registerMessage(id(), PacketVariablesReady.class, PacketVariablesReady::write, PacketVariablesReady::create, wrap(PacketVariablesReady::handle));
        net.registerMessage(id(), PacketFluidsReady.class, PacketFluidsReady::write, PacketFluidsReady::create, wrap(PacketFluidsReady::handle));
        net.registerMessage(id(), PacketGraphicsReady.class, PacketGraphicsReady::write, PacketGraphicsReady::create, wrap(PacketGraphicsReady::handle));
    }

    public static <T> void sendToPlayer(T packet, Player player) {
        INSTANCE.sendTo(packet, ((ServerPlayer)player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static <T> void sendToServer(T packet) {
        INSTANCE.sendToServer(packet);
    }
}
