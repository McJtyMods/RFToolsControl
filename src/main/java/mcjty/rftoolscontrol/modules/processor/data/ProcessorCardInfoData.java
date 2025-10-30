package mcjty.rftoolscontrol.modules.processor.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.rftoolscontrol.modules.processor.util.CardInfo;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Collections;
import java.util.List;

public record ProcessorCardInfoData(List<CardInfo> infos) {

    public static final ProcessorCardInfoData DEFAULT = new ProcessorCardInfoData(Collections.emptyList());

    public static final Codec<ProcessorCardInfoData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            CardInfo.CODEC.listOf().fieldOf("infos").forGetter(ProcessorCardInfoData::infos)
    ).apply(instance, ProcessorCardInfoData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ProcessorCardInfoData> STREAM_CODEC = StreamCodec.composite(
            CardInfo.STREAM_CODEC.apply(ByteBufCodecs.list()), ProcessorCardInfoData::infos,
            ProcessorCardInfoData::new
    );

    public ProcessorCardInfoData withInfos(List<CardInfo> infos) {
        return new ProcessorCardInfoData(infos);
    }
}
