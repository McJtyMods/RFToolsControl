package mcjty.rftoolscontrol.setup;

import mcjty.lib.network.Networking;
import mcjty.lib.network.PayloadContext;
import mcjty.rftoolscontrol.modules.processor.network.*;
import mcjty.rftoolscontrol.modules.programmer.network.PacketUpdateNBTItemInventoryProgrammer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public class RFToolsCtrlMessages {

    public static void registerMessages() {
        PayloadTypeRegistry.serverboundPlay().register(PacketGetLog.TYPE, PacketGetLog.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(PacketGetLog.TYPE, (packet, context) ->
                packet.handle(new PayloadContext(context.player(), context.server())));
        PayloadTypeRegistry.serverboundPlay().register(PacketGetVariables.TYPE, PacketGetVariables.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(PacketGetVariables.TYPE, (packet, context) ->
                packet.handle(new PayloadContext(context.player(), context.server())));
        PayloadTypeRegistry.serverboundPlay().register(PacketGetFluids.TYPE, PacketGetFluids.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(PacketGetFluids.TYPE, (packet, context) ->
                packet.handle(new PayloadContext(context.player(), context.server())));
        PayloadTypeRegistry.serverboundPlay().register(PacketVariableToServer.TYPE, PacketVariableToServer.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(PacketVariableToServer.TYPE, (packet, context) ->
                packet.handle(new PayloadContext(context.player(), context.server())));
        PayloadTypeRegistry.serverboundPlay().register(PacketUpdateNBTItemInventoryProgrammer.TYPE, PacketUpdateNBTItemInventoryProgrammer.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(PacketUpdateNBTItemInventoryProgrammer.TYPE, (packet, context) ->
                packet.handle(new PayloadContext(context.player(), context.server())));
        PayloadTypeRegistry.clientboundPlay().register(PacketLogReady.TYPE, PacketLogReady.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(PacketVariablesReady.TYPE, PacketVariablesReady.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(PacketFluidsReady.TYPE, PacketFluidsReady.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(PacketGraphicsReady.TYPE, PacketGraphicsReady.CODEC);
    }

    public static <T extends CustomPacketPayload> void sendToPlayer(T packet, Player player) {
        Networking.sendToPlayer(packet, player);
    }

    public static <T extends CustomPacketPayload> void sendToServer(T packet) {
        Networking.sendToServer(packet);
    }
}
