package mcjty.rftoolscontrol.setup;

import mcjty.lib.network.Networking;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.modules.processor.network.*;
import mcjty.rftoolscontrol.modules.programmer.network.PacketUpdateNBTItemInventoryProgrammer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.NetworkDirection;

public class RFToolsCtrlMessages {

    private static IPayloadRegistrar registrar;

    public static void registerMessages() {
        registrar = Networking.registrar(RFToolsControl.MODID)
                .versioned("1.0")
                .optional();

        // Server side
        registrar.play(PacketGetLog.class, PacketGetLog::create, handler -> handler.server(PacketGetLog::handle));
        registrar.play(PacketGetVariables.class, PacketGetVariables::create, handler -> handler.server(PacketGetVariables::handle));
        registrar.play(PacketGetFluids.class, PacketGetFluids::create, handler -> handler.server(PacketGetFluids::handle));
        registrar.play(PacketVariableToServer.class, PacketVariableToServer::create, handler -> handler.server(PacketVariableToServer::handle));
        registrar.play(PacketUpdateNBTItemInventoryProgrammer.class, PacketUpdateNBTItemInventoryProgrammer::create, handler -> handler.server(PacketUpdateNBTItemInventoryProgrammer::handle));

        // Client side
        registrar.play(PacketLogReady.class, PacketLogReady::create, handler -> handler.client(PacketLogReady::handle));
        registrar.play(PacketVariablesReady.class, PacketVariablesReady::create, handler -> handler.client(PacketVariablesReady::handle));
        registrar.play(PacketFluidsReady.class, PacketFluidsReady::create, handler -> handler.client(PacketFluidsReady::handle));
        registrar.play(PacketGraphicsReady.class, PacketGraphicsReady::create, handler -> handler.client(PacketGraphicsReady::handle));
    }

    public static <T> void sendToPlayer(T packet, Player player) {
        registrar.getChannel().sendTo(packet, ((ServerPlayer)player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static <T> void sendToServer(T packet) {
        registrar.getChannel().sendToServer(packet);
    }
}
