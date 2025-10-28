package mcjty.rftoolscontrol.modules.processor.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.rftoolscontrol.modules.processor.util.CardInfo;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Collections;
import java.util.List;

/**
 * Placeholder data component for processor card info.
 */
public record CardInfoData(List<CardInfo> infos) {

    public static final CardInfoData DEFAULT = new CardInfoData(Collections.emptyList());

    public static final Codec<CardInfoData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            CardInfo.CODEC.listOf().fieldOf("infos").forGetter(CardInfoData::infos)
    ).apply(instance, CardInfoData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CardInfoData> STREAM_CODEC = StreamCodec.composite(
            CardInfo.STREAM_CODEC.apply(ByteBufCodecs.list()), CardInfoData::infos,
            CardInfoData::new
    );
}
