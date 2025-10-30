package mcjty.rftoolscontrol.modules.processor.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.lib.varia.CompositeStreamCodec;
import mcjty.rftoolsbase.api.control.parameters.Parameter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Data component capturing core processor runtime state.
 */
public record ProcessorCoreData(Set<String> locks,
                                List<Parameter> variables,
                                List<WatchInfoEntry> watchInfos,
                                List<CoreEntry> cores,
                                int tickCount,
                                String lastException,
                                long lastExceptionTime) {

    public static final ProcessorCoreData DEFAULT = new ProcessorCoreData(Set.of(), List.of(), List.of(), List.of(), 0, "", 0L);

    public static final Codec<ProcessorCoreData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.listOf().fieldOf("locks").forGetter(data -> new ArrayList<>(data.locks())),
            Parameter.CODEC.listOf().fieldOf("variables").forGetter(ProcessorCoreData::variables),
            WatchInfoEntry.CODEC.listOf().fieldOf("watch_infos").forGetter(ProcessorCoreData::watchInfos),
            CoreEntry.CODEC.listOf().fieldOf("cores").forGetter(ProcessorCoreData::cores),
            Codec.INT.fieldOf("tickCount").forGetter(ProcessorCoreData::tickCount),
            Codec.STRING.fieldOf("lastException").forGetter(ProcessorCoreData::lastException),
            Codec.LONG.fieldOf("lastExceptionTime").forGetter(ProcessorCoreData::lastExceptionTime)
    ).apply(instance, (locks, variables, watchInfos, cores, tickCount, lastException, lastExceptionTime) ->
            new ProcessorCoreData(new LinkedHashSet<>(locks), variables, watchInfos, cores, tickCount, lastException, lastExceptionTime)));

    public static final StreamCodec<RegistryFriendlyByteBuf, ProcessorCoreData> STREAM_CODEC = CompositeStreamCodec.composite(
            ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.STRING_UTF8), data -> new ArrayList<>(data.locks()),
            Parameter.STREAM_CODEC.apply(ByteBufCodecs.collection(ArrayList::new)), ProcessorCoreData::variables,
            WatchInfoEntry.STREAM_CODEC.apply(ByteBufCodecs.collection(ArrayList::new)), ProcessorCoreData::watchInfos,
            CoreEntry.STREAM_CODEC.apply(ByteBufCodecs.collection(ArrayList::new)), ProcessorCoreData::cores,
            ByteBufCodecs.INT, ProcessorCoreData::tickCount,
            ByteBufCodecs.STRING_UTF8, ProcessorCoreData::lastException,
            ByteBufCodecs.VAR_LONG, ProcessorCoreData::lastExceptionTime,
            (locks, variables, watchInfos, cores, tickCount, lastException, lastExceptionTime) -> new ProcessorCoreData(new LinkedHashSet<>(locks), variables, watchInfos, cores, tickCount, lastException, lastExceptionTime)
    );

    // withXxx helpers
    public ProcessorCoreData withLocks(Set<String> locks) { return new ProcessorCoreData(locks, this.variables, this.watchInfos, this.cores, this.tickCount, this.lastException, this.lastExceptionTime); }
    public ProcessorCoreData withVariables(List<Parameter> variables) { return new ProcessorCoreData(this.locks, variables, this.watchInfos, this.cores, this.tickCount, this.lastException, this.lastExceptionTime); }
    public ProcessorCoreData withWatchInfos(List<WatchInfoEntry> watchInfos) { return new ProcessorCoreData(this.locks, this.variables, watchInfos, this.cores, this.tickCount, this.lastException, this.lastExceptionTime); }
    public ProcessorCoreData withCores(List<CoreEntry> cores) { return new ProcessorCoreData(this.locks, this.variables, this.watchInfos, cores, this.tickCount, this.lastException, this.lastExceptionTime); }
    public ProcessorCoreData withTickCount(int tickCount) { return new ProcessorCoreData(this.locks, this.variables, this.watchInfos, this.cores, tickCount, this.lastException, this.lastExceptionTime); }
    public ProcessorCoreData withLastException(String lastException) { return new ProcessorCoreData(this.locks, this.variables, this.watchInfos, this.cores, this.tickCount, lastException, this.lastExceptionTime); }
    public ProcessorCoreData withLastExceptionTime(long lastExceptionTime) { return new ProcessorCoreData(this.locks, this.variables, this.watchInfos, this.cores, this.tickCount, this.lastException, lastExceptionTime); }

    public record WatchInfoEntry(int index, boolean breakOnChange) {

        public static final Codec<WatchInfoEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("index").forGetter(WatchInfoEntry::index),
                Codec.BOOL.fieldOf("breakOnChange").forGetter(WatchInfoEntry::breakOnChange)
        ).apply(instance, WatchInfoEntry::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, WatchInfoEntry> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.INT, WatchInfoEntry::index,
                ByteBufCodecs.BOOL, WatchInfoEntry::breakOnChange,
                WatchInfoEntry::new
        );
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
