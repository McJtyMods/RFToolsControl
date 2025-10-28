package mcjty.rftoolscontrol.modules.processor.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.rftoolsbase.api.control.parameters.Parameter;
import mcjty.rftoolscontrol.modules.processor.logic.compiled.CompiledEvent;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import javax.annotation.Nullable;
import java.util.Optional;

public record QueuedEvent(int cardIndex,
                          CompiledEvent compiledEvent,
                          @Nullable String ticket,
                          @Nullable Parameter parameter) {

    public static final Codec<QueuedEvent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("cardIndex").forGetter(QueuedEvent::cardIndex),
            CompiledEvent.CODEC.fieldOf("compiledEvent").forGetter(QueuedEvent::compiledEvent),
            Codec.STRING.optionalFieldOf("ticket").forGetter(e -> Optional.ofNullable(e.ticket)),
            Parameter.CODEC.optionalFieldOf("parameter").forGetter(e -> Optional.ofNullable(e.parameter))
    ).apply(instance, (cardIndex, compiledEvent, ticket, parameter) ->
            new QueuedEvent(cardIndex, compiledEvent, ticket.orElse(null), parameter.orElse(null))));

    public static final StreamCodec<RegistryFriendlyByteBuf, QueuedEvent> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, QueuedEvent::cardIndex,
            CompiledEvent.STREAM_CODEC, QueuedEvent::compiledEvent,
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), e -> Optional.ofNullable(e.ticket),
            ByteBufCodecs.optional(Parameter.STREAM_CODEC), e -> Optional.ofNullable(e.parameter),
            (cardIndex, compiledEvent, ticket, parameter) ->
                    new QueuedEvent(cardIndex, compiledEvent, ticket.orElse(null), parameter.orElse(null))
    );

    public QueuedEvent(int cardIndex, CompiledEvent compiledEvent, @Nullable String ticket, @Nullable Parameter parameter) {
        this.cardIndex = cardIndex;
        this.compiledEvent = compiledEvent;
        this.ticket = ticket;
        this.parameter = parameter;
    }
}
