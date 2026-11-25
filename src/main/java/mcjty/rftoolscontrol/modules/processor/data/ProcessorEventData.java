package mcjty.rftoolscontrol.modules.processor.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import mcjty.rftoolscontrol.modules.processor.util.QueuedEvent;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Combined data component for queued and running processor events.
 */
public record ProcessorEventData(Queue<QueuedEvent> queuedEvents, Set<Pair<Integer, Integer>> runningEvents) {

    public static final ProcessorEventData DEFAULT = empty();

    private static final Codec<Pair<Integer, Integer>> RUNNING_EVENT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("card").forGetter(Pair::getLeft),
            Codec.INT.fieldOf("event").forGetter(Pair::getRight)
    ).apply(instance, Pair::of));

    public static final MapCodec<ProcessorEventData> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            QueuedEvent.CODEC.listOf().optionalFieldOf("queued", List.of()).forGetter(data -> new ArrayList<>(data.queuedEvents())),
            RUNNING_EVENT_CODEC.listOf().optionalFieldOf("running", List.of()).forGetter(data -> new ArrayList<>(data.runningEvents()))
    ).apply(instance, (queued, running) -> new ProcessorEventData(new ArrayDeque<>(queued), new HashSet<>(running))));
    public static final Codec<ProcessorEventData> CODEC = MAP_CODEC.codec();

    public static ProcessorEventData empty() {
        return new ProcessorEventData(new ArrayDeque<>(), new HashSet<>());
    }

    private static final StreamCodec<RegistryFriendlyByteBuf, Pair<Integer, Integer>> RUNNING_EVENT_STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.INT, Pair::getLeft, ByteBufCodecs.INT, Pair::getRight, Pair::of);

    public static final StreamCodec<RegistryFriendlyByteBuf, ProcessorEventData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(ArrayDeque::new, QueuedEvent.STREAM_CODEC), ProcessorEventData::queuedEvents,
            ByteBufCodecs.collection(HashSet::new, RUNNING_EVENT_STREAM_CODEC), ProcessorEventData::runningEvents,
            ProcessorEventData::new
    );

    // withXxx helpers
    public ProcessorEventData withQueuedEvents(Queue<QueuedEvent> queuedEvents) { return new ProcessorEventData(queuedEvents, this.runningEvents); }
    public ProcessorEventData withRunningEvents(Set<Pair<Integer, Integer>> runningEvents) { return new ProcessorEventData(this.queuedEvents, runningEvents); }
}
