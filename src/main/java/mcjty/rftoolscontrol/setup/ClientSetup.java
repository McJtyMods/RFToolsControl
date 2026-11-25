package mcjty.rftoolscontrol.setup;

import mcjty.lib.network.PayloadContext;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.modules.multitank.client.TankModelLoader;
import mcjty.rftoolscontrol.modules.processor.network.PacketFluidsReady;
import mcjty.rftoolscontrol.modules.processor.network.PacketGraphicsReady;
import mcjty.rftoolscontrol.modules.processor.network.PacketLogReady;
import mcjty.rftoolscontrol.modules.processor.network.PacketVariablesReady;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class ClientSetup implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        TankModelLoader.register();
        RFToolsControl.modules.initClient();
        ClientPlayNetworking.registerGlobalReceiver(PacketLogReady.TYPE, (packet, context) ->
                packet.handle(new PayloadContext(context.player(), context.client())));
        ClientPlayNetworking.registerGlobalReceiver(PacketVariablesReady.TYPE, (packet, context) ->
                packet.handle(new PayloadContext(context.player(), context.client())));
        ClientPlayNetworking.registerGlobalReceiver(PacketFluidsReady.TYPE, (packet, context) ->
                packet.handle(new PayloadContext(context.player(), context.client())));
        ClientPlayNetworking.registerGlobalReceiver(PacketGraphicsReady.TYPE, (packet, context) ->
                packet.handle(new PayloadContext(context.player(), context.client())));
    }
}
