package mcjty.rftoolscontrol.modules.programmer.network;

import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.modules.programmer.blocks.ProgrammerTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketUpdateNBTItemInventoryProgrammer(BlockPos pos, int slotIndex, ItemStack card) implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(RFToolsControl.MODID, "updatenbtiteminventoryprogrammer");
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

    protected boolean isValidBlock(Level world, BlockPos blockPos, BlockEntity tileEntity) {
        return tileEntity instanceof ProgrammerTileEntity;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Level world = ctx.player().getCommandSenderWorld();
            BlockEntity te = world.getBlockEntity(pos);
            if (te != null) {
                if (!isValidBlock(world, pos, te)) {
                    return;
                }
                IItemHandler handler = world.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
                if (handler != null) {
                    ItemStack stack = handler.getStackInSlot(slotIndex);
                    if (stack.getItem() == card.getItem()) {
                        stack.applyComponents(card.getComponents());
                    }
                    te.setChanged();
                }
            }
        });
    }
}
