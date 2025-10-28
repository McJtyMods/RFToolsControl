package mcjty.rftoolscontrol.modules.processor.logic.compiled;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record CompiledEvent(int index, boolean single) {
    
    public static final Codec<CompiledEvent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("index").forGetter(CompiledEvent::index),
            Codec.BOOL.fieldOf("single").forGetter(CompiledEvent::single)
    ).apply(instance, CompiledEvent::new));

    public static final StreamCodec<FriendlyByteBuf, CompiledEvent> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, CompiledEvent::index,
            ByteBufCodecs.BOOL, CompiledEvent::single,
            CompiledEvent::new
    );
}
