package mcjty.rftoolscontrol.modules.various.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Persistent data for the workbench block entity.
 */
public record WorkbenchData(int realItems) {

    public static final Codec<WorkbenchData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("realItems").forGetter(WorkbenchData::realItems)
    ).apply(instance, WorkbenchData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, WorkbenchData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, WorkbenchData::realItems,
            WorkbenchData::new);

    public static WorkbenchData createDefault() {
        return new WorkbenchData(0);
    }

    public WorkbenchData withRealItems(int realItems) {
        return new WorkbenchData(realItems);
    }
}
