package mcjty.rftoolscontrol.modules.processor.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.lib.varia.CompositeStreamCodec;
import mcjty.rftoolsbase.api.control.parameters.Parameter;
import mcjty.rftoolscontrol.modules.processor.util.WatchInfo;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Data component capturing core processor runtime state.
 */
public record ProcessorCoreData(Set<String> locks,
                                List<Parameter> variables,
                                List<WatchInfo> watchInfos,
                                List<CoreEntry> cores,
                                int tickCount,
                                String lastException,
                                long lastExceptionTime) {

    public static final ProcessorCoreData DEFAULT = new ProcessorCoreData(Set.of(), List.of(), List.of(), List.of(), 0, "", 0L);

    public ProcessorCoreData {
        Objects.requireNonNull(locks, "locks cannot be null");
        Objects.requireNonNull(variables, "variables cannot be null");
        Objects.requireNonNull(watchInfos, "watchInfos cannot be null");
        Objects.requireNonNull(cores, "cores cannot be null");
        locks = immutableLinkedSet(locks);
        variables = immutableListAllowNulls(variables);
        watchInfos = immutableListAllowNulls(watchInfos);
        cores = immutableListNoNulls(cores);
    }

    public static final Codec<ProcessorCoreData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.listOf().fieldOf("locks").forGetter(data -> new ArrayList<>(data.locks())),
            Parameter.CODEC.listOf().fieldOf("variables").forGetter(ProcessorCoreData::variables),
            WatchInfo.CODEC.listOf().fieldOf("watch_infos").forGetter(ProcessorCoreData::watchInfos),
            CoreEntry.CODEC.listOf().fieldOf("cores").forGetter(ProcessorCoreData::cores),
            Codec.INT.fieldOf("tickCount").forGetter(ProcessorCoreData::tickCount),
            Codec.STRING.fieldOf("lastException").forGetter(ProcessorCoreData::lastException),
            Codec.LONG.fieldOf("lastExceptionTime").forGetter(ProcessorCoreData::lastExceptionTime)
    ).apply(instance, (locks, variables, watchInfos, cores, tickCount, lastException, lastExceptionTime) ->
            new ProcessorCoreData(new LinkedHashSet<>(locks), variables, watchInfos, cores, tickCount, lastException, lastExceptionTime)));

    public static final StreamCodec<RegistryFriendlyByteBuf, ProcessorCoreData> STREAM_CODEC = CompositeStreamCodec.composite(
            ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.STRING_UTF8), data -> new ArrayList<>(data.locks()),
            Parameter.STREAM_CODEC.apply(ByteBufCodecs.collection(ArrayList::new)), ProcessorCoreData::variables,
            WatchInfo.STREAM_CODEC.apply(ByteBufCodecs.collection(ArrayList::new)), ProcessorCoreData::watchInfos,
            CoreEntry.STREAM_CODEC.apply(ByteBufCodecs.collection(ArrayList::new)), ProcessorCoreData::cores,
            ByteBufCodecs.INT, ProcessorCoreData::tickCount,
            ByteBufCodecs.STRING_UTF8, ProcessorCoreData::lastException,
            ByteBufCodecs.VAR_LONG, ProcessorCoreData::lastExceptionTime,
            (locks, variables, watchInfos, cores, tickCount, lastException, lastExceptionTime) -> new ProcessorCoreData(new LinkedHashSet<>(locks), variables, watchInfos, cores, tickCount, lastException, lastExceptionTime)
    );

    // withXxx helpers
    public ProcessorCoreData withLocks(Set<String> locks) { return new ProcessorCoreData(locks, this.variables, this.watchInfos, this.cores, this.tickCount, this.lastException, this.lastExceptionTime); }
    public ProcessorCoreData withVariables(List<Parameter> variables) { return new ProcessorCoreData(this.locks, variables, this.watchInfos, this.cores, this.tickCount, this.lastException, this.lastExceptionTime); }
    public ProcessorCoreData withWatchInfos(List<WatchInfo> watchInfos) { return new ProcessorCoreData(this.locks, this.variables, watchInfos, this.cores, this.tickCount, this.lastException, this.lastExceptionTime); }
    public ProcessorCoreData withCores(List<CoreEntry> cores) { return new ProcessorCoreData(this.locks, this.variables, this.watchInfos, cores, this.tickCount, this.lastException, this.lastExceptionTime); }
    public ProcessorCoreData withTickCount(int tickCount) { return new ProcessorCoreData(this.locks, this.variables, this.watchInfos, this.cores, tickCount, this.lastException, this.lastExceptionTime); }
    public ProcessorCoreData withLastException(String lastException) { return new ProcessorCoreData(this.locks, this.variables, this.watchInfos, this.cores, this.tickCount, lastException, this.lastExceptionTime); }
    public ProcessorCoreData withLastExceptionTime(long lastExceptionTime) { return new ProcessorCoreData(this.locks, this.variables, this.watchInfos, this.cores, this.tickCount, this.lastException, lastExceptionTime); }

    public Parameter variableAt(int index) {
        if (index < 0 || index >= variables.size()) {
            return null;
        }
        return variables.get(index);
    }

    public ProcessorCoreData withVariable(int index, Parameter value) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("index must be >= 0");
        }
        List<Parameter> copy = new ArrayList<>(variables);
        boolean requiresResize = index >= copy.size();
        if (!requiresResize && Objects.equals(copy.get(index), value)) {
            return this;
        }
        while (copy.size() <= index) {
            copy.add(null);
        }
        copy.set(index, value);
        return withVariables(copy);
    }

    public WatchInfo watchInfoAt(int index) {
        if (index < 0 || index >= watchInfos.size()) {
            return null;
        }
        return watchInfos.get(index);
    }

    public ProcessorCoreData withWatchInfo(int index, WatchInfo info) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("index must be >= 0");
        }
        List<WatchInfo> copy = new ArrayList<>(watchInfos);
        boolean requiresResize = index >= copy.size();
        if (!requiresResize && Objects.equals(copy.get(index), info)) {
            return this;
        }
        while (copy.size() <= index) {
            copy.add(null);
        }
        copy.set(index, info);
        return withWatchInfos(copy);
    }

    public boolean hasLock(String name) {
        return locks.contains(name);
    }

    public ProcessorCoreData withLockAdded(String name) {
        if (locks.contains(name)) {
            return this;
        }
        Set<String> copy = new LinkedHashSet<>(locks);
        copy.add(name);
        return withLocks(copy);
    }

    public ProcessorCoreData withLockRemoved(String name) {
        if (!locks.contains(name)) {
            return this;
        }
        Set<String> copy = new LinkedHashSet<>(locks);
        copy.remove(name);
        return withLocks(copy);
    }

    public CoreEntry coreAt(int index) {
        if (index < 0 || index >= cores.size()) {
            return null;
        }
        return cores.get(index);
    }

    public ProcessorCoreData withCore(int index, CoreEntry entry) {
        if (index < 0 || index >= cores.size()) {
            throw new IndexOutOfBoundsException("index out of bounds: " + index);
        }
        if (Objects.equals(cores.get(index), entry)) {
            return this;
        }
        Objects.requireNonNull(entry, "entry cannot be null");
        List<CoreEntry> copy = new ArrayList<>(cores);
        copy.set(index, entry);
        return withCores(copy);
    }

    private static <T> List<T> immutableListAllowNulls(List<T> source) {
        return Collections.unmodifiableList(new ArrayList<>(source));
    }

    private static <T> List<T> immutableListNoNulls(List<T> source) {
        ArrayList<T> copy = new ArrayList<>(source.size());
        for (T element : source) {
            copy.add(Objects.requireNonNull(element, "List element cannot be null"));
        }
        return Collections.unmodifiableList(copy);
    }

    private static <T> Set<T> immutableLinkedSet(Set<T> source) {
        return Collections.unmodifiableSet(new LinkedHashSet<>(source));
    }

    /**
     * Static representation of a CPU core installed in the processor. Runtime state lives in ProcessorTileEntity.cpuCores.
     */
    public record CoreEntry(int tier) {
        public static final Codec<CoreEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("tier").forGetter(CoreEntry::tier)
        ).apply(instance, CoreEntry::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, CoreEntry> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.INT, CoreEntry::tier,
                CoreEntry::new
        );
    }
}
