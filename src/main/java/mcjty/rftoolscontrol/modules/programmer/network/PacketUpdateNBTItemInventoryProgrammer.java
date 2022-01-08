package mcjty.rftoolscontrol.modules.programmer.network;

import mcjty.rftoolscontrol.modules.programmer.blocks.ProgrammerTileEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.items.CapabilityItemHandler;

import java.util.function.Supplier;

public class PacketUpdateNBTItemInventoryProgrammer {

    private BlockPos pos;
    private int slotIndex;
    private CompoundTag tagCompound;

    public PacketUpdateNBTItemInventoryProgrammer(FriendlyByteBuf buf) {
        pos = buf.readBlockPos();
        slotIndex = buf.readInt();
        tagCompound = buf.readNbt();
    }

    public PacketUpdateNBTItemInventoryProgrammer(BlockPos pos, int slotIndex, CompoundTag tagCompound) {
        this.pos = pos;
        this.slotIndex = slotIndex;
        this.tagCompound = tagCompound;
    }

    protected boolean isValidBlock(Level world, BlockPos blockPos, BlockEntity tileEntity) {
        return tileEntity instanceof ProgrammerTileEntity;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeInt(slotIndex);
        buf.writeNbt(tagCompound);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            Level world = ctx.getSender().getCommandSenderWorld();
            BlockEntity te = world.getBlockEntity(pos);
            if (te != null) {
                if (!isValidBlock(world, pos, te)) {
                    return;
                }
                te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(h -> {
                    ItemStack stack = h.getStackInSlot(slotIndex);
                    if (!stack.isEmpty()) {
                        stack.setTag(tagCompound);
                    }
                    te.setChanged();
                });
            }
        });
        ctx.setPacketHandled(true);
    }
}
