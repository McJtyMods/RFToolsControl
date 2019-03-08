package mcjty.rftoolscontrol.network;

import mcjty.lib.network.PacketHandler;
import mcjty.lib.thirteen.ChannelBuilder;
import mcjty.lib.thirteen.SimpleChannel;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.blocks.programmer.PacketUpdateNBTItemInventoryProgrammer;
import mcjty.rftoolscontrol.items.craftingcard.PacketUpdateNBTItemCard;
import mcjty.rftoolscontrol.jei.PacketSendRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

public class RFToolsCtrlMessages {
    public static SimpleNetworkWrapper INSTANCE;

    public static void registerMessages(String name) {
        SimpleChannel net = ChannelBuilder
                .named(new ResourceLocation(RFToolsControl.MODID, name))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE = net.getNetwork();

        // Server side
        net.registerMessageServer(id(), PacketGetLog.class, PacketGetLog::toBytes, PacketGetLog::new, PacketGetLog::handle);
        net.registerMessageServer(id(), PacketGetDebugLog.class, PacketGetDebugLog::toBytes, PacketGetDebugLog::new, PacketGetDebugLog::handle);
        net.registerMessageServer(id(), PacketGetVariables.class, PacketGetVariables::toBytes, PacketGetVariables::new, PacketGetVariables::handle);
        net.registerMessageServer(id(), PacketGetFluids.class, PacketGetFluids::toBytes, PacketGetFluids::new, PacketGetFluids::handle);
        net.registerMessageServer(id(), PacketGetTankFluids.class, PacketGetTankFluids::toBytes, PacketGetTankFluids::new, PacketGetTankFluids::handle);
        net.registerMessageServer(id(), PacketGetCraftableItems.class, PacketGetCraftableItems::toBytes, PacketGetCraftableItems::new, PacketGetCraftableItems::handle);
        net.registerMessageServer(id(), PacketGetRequests.class, PacketGetRequests::toBytes, PacketGetRequests::new, PacketGetRequests::handle);
        net.registerMessageServer(id(), PacketSendRecipe.class, PacketSendRecipe::toBytes, PacketSendRecipe::new, PacketSendRecipe::handle);
        net.registerMessageServer(id(), PacketItemNBTToServer.class, PacketItemNBTToServer::toBytes, PacketItemNBTToServer::new, PacketItemNBTToServer::handle);
        net.registerMessageServer(id(), PacketVariableToServer.class, PacketVariableToServer::toBytes, PacketVariableToServer::new, PacketVariableToServer::handle);
        net.registerMessageServer(id(), PacketUpdateNBTItemInventoryProgrammer.class, PacketUpdateNBTItemInventoryProgrammer::toBytes, PacketUpdateNBTItemInventoryProgrammer::new, PacketUpdateNBTItemInventoryProgrammer::handle);
        net.registerMessageServer(id(), PacketUpdateNBTItemCard.class, PacketUpdateNBTItemCard::toBytes, PacketUpdateNBTItemCard::new, PacketUpdateNBTItemCard::handle);

        // Client side
        net.registerMessageClient(id(), PacketLogReady.class, PacketLogReady::toBytes, PacketLogReady::new, PacketLogReady::handle);
        net.registerMessageClient(id(), PacketVariablesReady.class, PacketVariablesReady::toBytes, PacketVariablesReady::new, PacketVariablesReady::handle);
        net.registerMessageClient(id(), PacketFluidsReady.class, PacketFluidsReady::toBytes, PacketFluidsReady::new, PacketFluidsReady::handle);
        net.registerMessageClient(id(), PacketTankFluidsReady.class, PacketTankFluidsReady::toBytes, PacketTankFluidsReady::new, PacketTankFluidsReady::handle);
        net.registerMessageClient(id(), PacketCraftableItemsReady.class, PacketCraftableItemsReady::toBytes, PacketCraftableItemsReady::new, PacketCraftableItemsReady::handle);
        net.registerMessageClient(id(), PacketRequestsReady.class, PacketRequestsReady::toBytes, PacketRequestsReady::new, PacketRequestsReady::handle);
        net.registerMessageClient(id(), PacketGraphicsReady.class, PacketGraphicsReady::toBytes, PacketGraphicsReady::new, PacketGraphicsReady::handle);
    }

    private static int id() {
        return PacketHandler.nextPacketID();
    }
}
