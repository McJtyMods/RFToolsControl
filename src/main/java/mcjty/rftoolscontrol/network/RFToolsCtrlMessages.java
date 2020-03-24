package mcjty.rftoolscontrol.network;

import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.blocks.programmer.PacketUpdateNBTItemInventoryProgrammer;
import mcjty.rftoolscontrol.compat.jei.PacketSendRecipe;
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
        net.registerMessage(id(), PacketGetDebugLog.class, PacketGetDebugLog::toBytes, PacketGetDebugLog::new, PacketGetDebugLog::handle);
        net.registerMessage(id(), PacketGetVariables.class, PacketGetVariables::toBytes, PacketGetVariables::new, PacketGetVariables::handle);
        net.registerMessage(id(), PacketGetFluids.class, PacketGetFluids::toBytes, PacketGetFluids::new, PacketGetFluids::handle);
        net.registerMessage(id(), PacketGetTankFluids.class, PacketGetTankFluids::toBytes, PacketGetTankFluids::new, PacketGetTankFluids::handle);
        net.registerMessage(id(), PacketGetCraftableItems.class, PacketGetCraftableItems::toBytes, PacketGetCraftableItems::new, PacketGetCraftableItems::handle);
        net.registerMessage(id(), PacketGetRequests.class, PacketGetRequests::toBytes, PacketGetRequests::new, PacketGetRequests::handle);
        net.registerMessage(id(), PacketSendRecipe.class, PacketSendRecipe::toBytes, PacketSendRecipe::new, PacketSendRecipe::handle);
        net.registerMessage(id(), PacketVariableToServer.class, PacketVariableToServer::toBytes, PacketVariableToServer::new, PacketVariableToServer::handle);
        net.registerMessage(id(), PacketUpdateNBTItemInventoryProgrammer.class, PacketUpdateNBTItemInventoryProgrammer::toBytes, PacketUpdateNBTItemInventoryProgrammer::new, PacketUpdateNBTItemInventoryProgrammer::handle);

        // Client side
        net.registerMessage(id(), PacketLogReady.class, PacketLogReady::toBytes, PacketLogReady::new, PacketLogReady::handle);
        net.registerMessage(id(), PacketVariablesReady.class, PacketVariablesReady::toBytes, PacketVariablesReady::new, PacketVariablesReady::handle);
        net.registerMessage(id(), PacketFluidsReady.class, PacketFluidsReady::toBytes, PacketFluidsReady::new, PacketFluidsReady::handle);
        net.registerMessage(id(), PacketTankFluidsReady.class, PacketTankFluidsReady::toBytes, PacketTankFluidsReady::new, PacketTankFluidsReady::handle);
        net.registerMessage(id(), PacketCraftableItemsReady.class, PacketCraftableItemsReady::toBytes, PacketCraftableItemsReady::new, PacketCraftableItemsReady::handle);
        net.registerMessage(id(), PacketRequestsReady.class, PacketRequestsReady::toBytes, PacketRequestsReady::new, PacketRequestsReady::handle);
        net.registerMessage(id(), PacketGraphicsReady.class, PacketGraphicsReady::toBytes, PacketGraphicsReady::new, PacketGraphicsReady::handle);
    }
}
