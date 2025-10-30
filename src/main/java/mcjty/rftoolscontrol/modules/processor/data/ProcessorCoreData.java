package mcjty.rftoolscontrol.modules.processor.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.rftoolsbase.api.control.parameters.Parameter;
import mcjty.lib.varia.CompositeStreamCodec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Data component capturing core processor runtime state.
 */
public record ProcessorCoreData(Set<String> locks,
                                List<VariableEntry> variables,
                                List<WatchInfoEntry> watchInfos,
                                List<CompoundTag> cores,
                                int tickCount,
                                String channel,
                                String lastException,
                                long lastExceptionTime) {

    public static final ProcessorCoreData DEFAULT = new ProcessorCoreData(Set.of(), List.of(), List.of(), List.of(), 0, "", "", 0L);

    public static final Codec<ProcessorCoreData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.listOf().fieldOf("locks").forGetter(data -> new ArrayList<>(data.locks())),
            VariableEntry.CODEC.listOf().fieldOf("variables").forGetter(ProcessorCoreData::variables),
            WatchInfoEntry.CODEC.listOf().fieldOf("watch_infos").forGetter(ProcessorCoreData::watchInfos),
            CompoundTag.CODEC.listOf().fieldOf("cores").forGetter(ProcessorCoreData::cores),
            Codec.INT.fieldOf("tickCount").forGetter(ProcessorCoreData::tickCount),
            Codec.STRING.fieldOf("channel").forGetter(ProcessorCoreData::channel),
            Codec.STRING.fieldOf("lastException").forGetter(ProcessorCoreData::lastException),
            Codec.LONG.fieldOf("lastExceptionTime").forGetter(ProcessorCoreData::lastExceptionTime)
    ).apply(instance, (locks, variables, watchInfos, cores, tickCount, channel, lastException, lastExceptionTime) ->
            new ProcessorCoreData(new LinkedHashSet<>(locks), variables, watchInfos, cores, tickCount, channel, lastException, lastExceptionTime)));

    public static final StreamCodec<RegistryFriendlyByteBuf, ProcessorCoreData> STREAM_CODEC = CompositeStreamCodec.composite(
            ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.STRING_UTF8), data -> new ArrayList<>(data.locks()),
            VariableEntry.STREAM_CODEC.apply(ByteBufCodecs.collection(ArrayList::new)), ProcessorCoreData::variables,
            WatchInfoEntry.STREAM_CODEC.apply(ByteBufCodecs.collection(ArrayList::new)), ProcessorCoreData::watchInfos,
            ByteBufCodecs.COMPOUND_TAG.apply(ByteBufCodecs.collection(ArrayList::new)), ProcessorCoreData::cores,
            ByteBufCodecs.INT, ProcessorCoreData::tickCount,
            ByteBufCodecs.STRING_UTF8, ProcessorCoreData::channel,
            ByteBufCodecs.STRING_UTF8, ProcessorCoreData::lastException,
            ByteBufCodecs.VAR_LONG, ProcessorCoreData::lastExceptionTime,
            (locks, variables, watchInfos, cores, tickCount, channel, lastException, lastExceptionTime) -> new ProcessorCoreData(new LinkedHashSet<>(locks), variables, watchInfos, cores, tickCount, channel, lastException, lastExceptionTime)
    );

    // withXxx helpers
    public ProcessorCoreData withLocks(Set<String> locks) { return new ProcessorCoreData(locks, this.variables, this.watchInfos, this.cores, this.tickCount, this.channel, this.lastException, this.lastExceptionTime); }
    public ProcessorCoreData withVariables(List<VariableEntry> variables) { return new ProcessorCoreData(this.locks, variables, this.watchInfos, this.cores, this.tickCount, this.channel, this.lastException, this.lastExceptionTime); }
    public ProcessorCoreData withWatchInfos(List<WatchInfoEntry> watchInfos) { return new ProcessorCoreData(this.locks, this.variables, watchInfos, this.cores, this.tickCount, this.channel, this.lastException, this.lastExceptionTime); }
    public ProcessorCoreData withCores(List<CompoundTag> cores) { return new ProcessorCoreData(this.locks, this.variables, this.watchInfos, cores, this.tickCount, this.channel, this.lastException, this.lastExceptionTime); }
    public ProcessorCoreData withTickCount(int tickCount) { return new ProcessorCoreData(this.locks, this.variables, this.watchInfos, this.cores, tickCount, this.channel, this.lastException, this.lastExceptionTime); }
    public ProcessorCoreData withChannel(String channel) { return new ProcessorCoreData(this.locks, this.variables, this.watchInfos, this.cores, this.tickCount, channel, this.lastException, this.lastExceptionTime); }
    public ProcessorCoreData withLastException(String lastException) { return new ProcessorCoreData(this.locks, this.variables, this.watchInfos, this.cores, this.tickCount, this.channel, lastException, this.lastExceptionTime); }
    public ProcessorCoreData withLastExceptionTime(long lastExceptionTime) { return new ProcessorCoreData(this.locks, this.variables, this.watchInfos, this.cores, this.tickCount, this.channel, this.lastException, lastExceptionTime); }

    public record VariableEntry(int index, Parameter parameter) {

        public static final Codec<VariableEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("index").forGetter(VariableEntry::index),
                Parameter.CODEC.fieldOf("parameter").forGetter(VariableEntry::parameter)
        ).apply(instance, VariableEntry::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, VariableEntry> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.INT, VariableEntry::index,
                Parameter.STREAM_CODEC, VariableEntry::parameter,
                VariableEntry::new
        );

        public VariableEntry {
            Objects.requireNonNull(parameter, "parameter");
        }
    }

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
}
