package mcjty.rftoolscontrol.modules.processor.data;

import com.mojang.serialization.Codec;
import mcjty.rftoolscontrol.modules.processor.util.QueuedEvent;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * Combined data component for queued and running processor events.
 */
public record ProcessorEventData(Queue<QueuedEvent> queuedEvents, Set<Pair<Integer, Integer>> runningEvents) {

    public static final ProcessorEventData DEFAULT = empty();

    public static final Codec<ProcessorEventData> CODEC = Codec.pair(
            QueuedEvent.CODEC.listOf(),
            Codec.pair(Codec.INT.listOf(), Codec.INT.listOf())
    ).xmap(
            pair -> new ProcessorEventData(
                    new LinkedList<>(pair.getFirst()),
                    zipToPairs(pair.getSecond().getFirst(), pair.getSecond().getSecond())
            ),
            data -> com.mojang.datafixers.util.Pair.of(
                    new ArrayList<>(data.queuedEvents()),
                    com.mojang.datafixers.util.Pair.of(
                            data.runningEvents().stream().map(Pair::getLeft).toList(),
                            data.runningEvents().stream().map(Pair::getRight).toList()
                    )
            )
    );

    public static ProcessorEventData empty() {
        return new ProcessorEventData(new ArrayDeque<>(), new HashSet<>());
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, ProcessorEventData> STREAM_CODEC = StreamCodec.of(
            (buf, value) -> {
                buf.writeCollection(value.queuedEvents(), (b, event) -> QueuedEvent.STREAM_CODEC.encode((RegistryFriendlyByteBuf) b, event));
                buf.writeCollection(value.runningEvents(), (b, pair) -> {
                    b.writeInt(pair.getLeft());
                    b.writeInt(pair.getRight());
                });
            },
            buf -> new ProcessorEventData(
                    new LinkedList<>(buf.readCollection(
                            ArrayDeque::new,
                            b -> QueuedEvent.STREAM_CODEC.decode((RegistryFriendlyByteBuf) b)
                    )),
                    buf.readCollection(
                            HashSet::new,
                            b -> Pair.of(b.readInt(), b.readInt())
                    )
            )
    );

    private static Set<Pair<Integer, Integer>> zipToPairs(List<Integer> left, List<Integer> right) {
        int size = Math.min(left.size(), right.size());
        Set<Pair<Integer, Integer>> result = new HashSet<>();
        for (int i = 0; i < size; i++) {
            result.add(Pair.of(left.get(i), right.get(i)));
        }
        return result;
    }
}
