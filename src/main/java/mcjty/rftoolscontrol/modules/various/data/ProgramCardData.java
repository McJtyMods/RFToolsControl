package mcjty.rftoolscontrol.modules.various.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Persistent data for the node block entity, tracking the configured channel,
 * node name, and linked processor position.
 */
public record ProgramCardData(@Nonnull String name) {

    public static final Codec<ProgramCardData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(data -> data.name)
    ).apply(instance, ProgramCardData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ProgramCardData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, data -> data.name,
            ProgramCardData::new);

    public static ProgramCardData createDefault() {
        return new ProgramCardData("");
    }

    public ProgramCardData withName(@Nullable String name) {
        return new ProgramCardData(name);
    }
}
