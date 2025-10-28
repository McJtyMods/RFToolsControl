package mcjty.rftoolscontrol.modules.processor.data;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Placeholder data component for processor running events.
 */
public record RunningEventsData(Set<Pair<Integer, Integer>> events) {

    public static final RunningEventsData DEFAULT = new RunningEventsData(Collections.emptySet());

    public static final Codec<RunningEventsData> CODEC = Codec.pair(
            Codec.INT.listOf(),
            Codec.INT.listOf()
    ).xmap(
            pair -> new RunningEventsData(
                    IntStream.range(0, pair.getFirst().size())
                            .mapToObj(i -> Pair.of(pair.getFirst().get(i), pair.getSecond().get(i)))
                            .collect(Collectors.toSet())),
            data -> com.mojang.datafixers.util.Pair.of(
                    data.events().stream().map(Pair::getLeft).toList(),
                    data.events().stream().map(Pair::getRight).toList()
            )
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, RunningEventsData> STREAM_CODEC = StreamCodec.of(
            (buf, value) -> {
                buf.writeCollection(value.events(), (b, pair) -> {
                    b.writeInt(pair.getLeft());
                    b.writeInt(pair.getRight());
                });
            },
            buf -> new RunningEventsData(buf.readCollection(
                    HashSet::new,
                    b -> Pair.of(b.readInt(), b.readInt())
            ))
    );
}
