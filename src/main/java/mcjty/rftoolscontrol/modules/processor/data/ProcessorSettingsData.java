package mcjty.rftoolscontrol.modules.processor.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.lib.varia.CompositeStreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Data component capturing processor user settings like HUD visibility and exclusive mode.
 */
public record ProcessorSettingsData(boolean exclusive,
                                    int showHud) {

    public static final ProcessorSettingsData DEFAULT = new ProcessorSettingsData(false, 0);

    public static final Codec<ProcessorSettingsData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("exclusive").forGetter(ProcessorSettingsData::exclusive),
            Codec.INT.fieldOf("showHud").forGetter(ProcessorSettingsData::showHud)
    ).apply(instance, ProcessorSettingsData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ProcessorSettingsData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ProcessorSettingsData::exclusive,
            ByteBufCodecs.INT, ProcessorSettingsData::showHud,
            ProcessorSettingsData::new
    );

    // withXxx helpers
    public ProcessorSettingsData withExclusive(boolean exclusive) {
        return new ProcessorSettingsData(exclusive, this.showHud);
    }

    public ProcessorSettingsData withShowHud(int showHud) {
        return new ProcessorSettingsData(this.exclusive, showHud);
    }
}
