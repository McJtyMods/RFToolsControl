package mcjty.rftoolscontrol.modules.processor.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.rftoolscontrol.modules.processor.util.WaitForItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Placeholder data component mirroring crafting-related processor state.
 */
public record ProcessorCraftingData(List<WaitForItem> waitingForItems, Set<BlockPos> craftingStations) {

    public static final ProcessorCraftingData DEFAULT = new ProcessorCraftingData(Collections.emptyList(), Collections.emptySet());

    public static final Codec<ProcessorCraftingData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            WaitForItem.CODEC.listOf().fieldOf("waitingForItems").forGetter(ProcessorCraftingData::waitingForItems),
            BlockPos.CODEC.listOf().fieldOf("craftingStations").forGetter(data -> new ArrayList<>(data.craftingStations()))
    ).apply(instance, (waiting, stations) -> new ProcessorCraftingData(waiting, new HashSet<>(stations))));

    public static final StreamCodec<RegistryFriendlyByteBuf, ProcessorCraftingData> STREAM_CODEC = StreamCodec.composite(
            WaitForItem.STREAM_CODEC.apply(ByteBufCodecs.list()), ProcessorCraftingData::waitingForItems,
            BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list()), data -> new ArrayList<>(data.craftingStations()),
            (waiting, stations) -> new ProcessorCraftingData(waiting, new HashSet<>(stations))
    );
}
