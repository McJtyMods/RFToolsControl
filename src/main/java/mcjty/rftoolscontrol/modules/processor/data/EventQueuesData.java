package mcjty.rftoolscontrol.modules.processor.data;

import com.mojang.serialization.Codec;
import mcjty.rftoolscontrol.modules.processor.util.QueuedEvent;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Placeholder data component for processor event queues.
 */
public record EventQueuesData(Queue<QueuedEvent> events) {

    public static final EventQueuesData DEFAULT = new EventQueuesData(new ArrayDeque<>());

    public static final Codec<EventQueuesData> CODEC = Codec.list(QueuedEvent.CODEC)
            .xmap(
                    list -> new EventQueuesData(new LinkedList<>(list)),
                    data -> new ArrayList<>(data.events())
            );

    public static final StreamCodec<RegistryFriendlyByteBuf, EventQueuesData> STREAM_CODEC = StreamCodec.of(
            (buf, value) -> {
                buf.writeCollection(value.events(), (b, event) -> QueuedEvent.STREAM_CODEC.encode((RegistryFriendlyByteBuf) b, event));
            },
            buf -> new EventQueuesData(
                    new LinkedList<>(buf.readCollection(
                            ArrayDeque::new,
                            b -> QueuedEvent.STREAM_CODEC.decode((RegistryFriendlyByteBuf) b)
                    ))
            )
    );
}
