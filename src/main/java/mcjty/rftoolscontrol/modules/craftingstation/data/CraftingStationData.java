package mcjty.rftoolscontrol.modules.craftingstation.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record CraftingStationData(int craftId) {

    public static final Codec<CraftingStationData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("craftId").forGetter(CraftingStationData::craftId)
    ).apply(instance, CraftingStationData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CraftingStationData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, CraftingStationData::craftId,
            CraftingStationData::new
    );

    public static CraftingStationData createDefault() {
        return new CraftingStationData(0);
    }

    public CraftingStationData withCraftId(int craftId) {
        return new CraftingStationData(craftId);
    }
}
