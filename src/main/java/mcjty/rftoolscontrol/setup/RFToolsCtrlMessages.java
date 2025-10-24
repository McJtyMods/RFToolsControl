package mcjty.rftoolscontrol.setup;

import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.modules.processor.network.*;
import mcjty.rftoolscontrol.modules.programmer.network.PacketUpdateNBTItemInventoryProgrammer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class RFToolsCtrlMessages {

    public static void registerMessages(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(RFToolsControl.MODID)
                .versioned("1.0")
                .optional();

        // Server side
        registrar.playToServer(PacketGetLog.TYPE, PacketGetLog.CODEC, PacketGetLog::handle);
        registrar.playToServer(PacketGetVariables.TYPE, PacketGetVariables.CODEC, PacketGetVariables::handle);
        registrar.playToServer(PacketGetFluids.TYPE, PacketGetFluids.CODEC, PacketGetFluids::handle);
        registrar.playToServer(PacketVariableToServer.TYPE, PacketVariableToServer.CODEC, PacketVariableToServer::handle);
        registrar.playToServer(PacketUpdateNBTItemInventoryProgrammer.TYPE, PacketUpdateNBTItemInventoryProgrammer.CODEC, PacketUpdateNBTItemInventoryProgrammer::handle);

        // Client side
        registrar.playToClient(PacketLogReady.TYPE, PacketLogReady.CODEC, PacketLogReady::handle);
        registrar.playToClient(PacketVariablesReady.TYPE, PacketVariablesReady.CODEC, PacketVariablesReady::handle);
        registrar.playToClient(PacketFluidsReady.TYPE, PacketFluidsReady.CODEC, PacketFluidsReady::handle);
        registrar.playToClient(PacketGraphicsReady.TYPE, PacketGraphicsReady.CODEC, PacketGraphicsReady::handle);
    }

    public static <T extends CustomPacketPayload> void sendToPlayer(T packet, Player player) {
        PacketDistributor.sendToPlayer((ServerPlayer) player, packet);
    }

    public static <T extends CustomPacketPayload> void sendToServer(T packet) {
        PacketDistributor.sendToServer(packet);
    }
}
