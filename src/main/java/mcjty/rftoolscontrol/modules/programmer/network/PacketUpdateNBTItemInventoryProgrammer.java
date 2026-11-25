package mcjty.rftoolscontrol.modules.programmer.network;

import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.modules.programmer.blocks.ProgrammerTileEntity;
import mcjty.rftoolscontrol.modules.various.VariousModule;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketUpdateNBTItemInventoryProgrammer(BlockPos pos, int slotIndex, ItemStack card) implements CustomPacketPayload {

    public static final Identifier ID = Identifier.fromNamespaceAndPath(RFToolsControl.MODID, "updatenbtiteminventoryprogrammer");
    public static final CustomPacketPayload.Type<PacketUpdateNBTItemInventoryProgrammer> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketUpdateNBTItemInventoryProgrammer> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PacketUpdateNBTItemInventoryProgrammer::pos,
            ByteBufCodecs.INT, PacketUpdateNBTItemInventoryProgrammer::slotIndex,
            ItemStack.OPTIONAL_STREAM_CODEC, PacketUpdateNBTItemInventoryProgrammer::card,
            PacketUpdateNBTItemInventoryProgrammer::new
    );

    public static PacketUpdateNBTItemInventoryProgrammer create(BlockPos blockPos, int slot, ItemStack card) {
        return new PacketUpdateNBTItemInventoryProgrammer(blockPos, slot, card);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (slotIndex != ProgrammerTileEntity.SLOT_DUMMY && slotIndex != ProgrammerTileEntity.SLOT_CARD) {
                return;
            }
            var player = ctx.player();
            if (!(player.level().getBlockEntity(pos) instanceof ProgrammerTileEntity programmer)
                    || !player.isWithinBlockInteractionRange(pos, 0) || !programmer.canPlayerAccess(player)) {
                return;
            }
            var items = programmer.getItems();
            ItemStack existing = items.getStackInSlot(slotIndex);
            if (existing.isEmpty() || !ItemStack.isSameItem(existing, card) || !card.is(VariousModule.PROGRAM_CARD.get())) {
                return;
            }
            ItemStack updated = existing.copy();
            updated.set(VariousModule.PROGRAM_CARD_DATA.get(), card.get(VariousModule.PROGRAM_CARD_DATA.get()));
            updated.set(VariousModule.PROGRAM_CARD_NAME.get(), card.get(VariousModule.PROGRAM_CARD_NAME.get()));
            items.setStackInSlot(slotIndex, updated);
            programmer.setChanged();
        });
    }
}
