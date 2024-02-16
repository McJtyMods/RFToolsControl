package mcjty.rftoolscontrol.modules.programmer.network;

import mcjty.lib.network.CustomPacketPayload;
import mcjty.lib.network.PlayPayloadContext;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.modules.programmer.blocks.ProgrammerTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

public record PacketUpdateNBTItemInventoryProgrammer(BlockPos pos, int slotIndex, CompoundTag tagCompound) implements CustomPacketPayload {

    public static final ResourceLocation ID = new ResourceLocation(RFToolsControl.MODID, "updatenbtiteminventoryprogrammer");

    public static PacketUpdateNBTItemInventoryProgrammer create(FriendlyByteBuf buf) {
        return new PacketUpdateNBTItemInventoryProgrammer(buf.readBlockPos(), buf.readInt(), buf.readNbt());
    }

    public static PacketUpdateNBTItemInventoryProgrammer create(BlockPos blockPos, int slot, CompoundTag tag) {
        return new PacketUpdateNBTItemInventoryProgrammer(blockPos, slot, tag);
    }

    protected boolean isValidBlock(Level world, BlockPos blockPos, BlockEntity tileEntity) {
        return tileEntity instanceof ProgrammerTileEntity;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeInt(slotIndex);
        buf.writeNbt(tagCompound);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public void handle(PlayPayloadContext ctx) {
        ctx.workHandler().submitAsync(() -> {
            ctx.player().ifPresent(player -> {
                Level world = player.getCommandSenderWorld();
                BlockEntity te = world.getBlockEntity(pos);
                if (te != null) {
                    if (!isValidBlock(world, pos, te)) {
                        return;
                    }
                    te.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(h -> {
                        ItemStack stack = h.getStackInSlot(slotIndex);
                        if (!stack.isEmpty()) {
                            stack.setTag(tagCompound);
                        }
                        te.setChanged();
                    });
                }
            });
        });
    }
}
