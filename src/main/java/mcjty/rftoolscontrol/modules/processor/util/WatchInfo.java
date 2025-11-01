package mcjty.rftoolscontrol.modules.processor.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record WatchInfo(boolean breakOnChange) {
    public static final Codec<WatchInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("breakOnChange").forGetter(WatchInfo::breakOnChange)
    ).apply(instance, WatchInfo::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, WatchInfo> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, WatchInfo::breakOnChange,
            WatchInfo::new
    );
}
