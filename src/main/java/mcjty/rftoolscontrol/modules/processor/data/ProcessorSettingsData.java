package mcjty.rftoolscontrol.modules.processor.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * Data component capturing processor user settings like HUD visibility and exclusive mode.
 */
public record ProcessorSettingsData(boolean exclusive,
                                    HudMode showHud) {

    public static final ProcessorSettingsData DEFAULT = new ProcessorSettingsData(false, HudMode.OFF);

    public static final Codec<ProcessorSettingsData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("exclusive").forGetter(ProcessorSettingsData::exclusive),
            HudMode.CODEC.fieldOf("showHud").forGetter(ProcessorSettingsData::showHud)
    ).apply(instance, ProcessorSettingsData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ProcessorSettingsData> STREAM_CODEC = StreamCodec.composite(
            net.minecraft.network.codec.ByteBufCodecs.BOOL, ProcessorSettingsData::exclusive,
            HudMode.STREAM_CODEC, ProcessorSettingsData::showHud,
            ProcessorSettingsData::new
    );

    public ProcessorSettingsData withExclusive(boolean exclusive) {
        return new ProcessorSettingsData(exclusive, this.showHud);
    }

    public ProcessorSettingsData withShowHud(HudMode showHud) {
        return new ProcessorSettingsData(this.exclusive, showHud);
    }
}
