package mcjty.rftoolscontrol.modules.processor.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.rftoolsbase.api.control.parameters.Inventory;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public record WaitForItem(@Nonnull String ticket, ItemStack itemStack,
                          @Nullable Inventory inventory) {

    public static final Codec<WaitForItem> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("ticket").forGetter(WaitForItem::ticket),
            ItemStack.OPTIONAL_CODEC.optionalFieldOf("item").forGetter(waitForItem ->
                    waitForItem.itemStack().isEmpty() ? Optional.empty() : Optional.of(waitForItem.itemStack())),
            Inventory.CODEC.optionalFieldOf("inventory").forGetter(waitForItem ->
                    Optional.ofNullable(waitForItem.inventory()))
    ).apply(instance, (ticket, item, inventory) ->
            new WaitForItem(ticket, item.orElse(ItemStack.EMPTY), inventory.orElse(null))));

    public static final StreamCodec<RegistryFriendlyByteBuf, WaitForItem> STREAM_CODEC = StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, WaitForItem::ticket,
                    ItemStack.OPTIONAL_STREAM_CODEC, WaitForItem::itemStack,
                    ByteBufCodecs.optional(Inventory.STREAM_CODEC), waitForItem -> Optional.ofNullable(waitForItem.inventory()),
                    (ticket, stack, inventory) -> new WaitForItem(ticket, stack, inventory.orElse(null))
            );

    public WaitForItem(@Nonnull String ticket, ItemStack itemStack, @Nullable Inventory inventory) {
        this.ticket = ticket;
        this.itemStack = itemStack;
        this.inventory = inventory;
    }
}
