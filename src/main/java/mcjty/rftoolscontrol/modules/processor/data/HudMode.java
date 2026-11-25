package mcjty.rftoolscontrol.modules.processor.data;

import com.mojang.serialization.Codec;
import java.util.Locale;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

/**
 * HUD display modes for the Processor.
 */
public enum HudMode implements StringRepresentable {
    OFF("Off"),
    LOG("Log"),
    DB("Db"),
    GFX("Gfx");

    private final String name;

    HudMode(String name) {
        this.name = name;
    }

    // Use the standard convenience codec for StringRepresentable enums
    public static final Codec<HudMode> CODEC = StringRepresentable.fromEnum(HudMode::values);
    public static final StreamCodec<FriendlyByteBuf, HudMode> STREAM_CODEC = StreamCodec.of(FriendlyByteBuf::writeEnum, buffer -> buffer.readEnum(HudMode.class));

    public static HudMode fromOrdinal(int ord) {
        HudMode[] values = values();
        if (ord < 0 || ord >= values.length) {
            return OFF;
        }
        return values[ord];
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String getName() {
        return name;
    }

    public static HudMode stringToMode(String name) {
        if ("Off".equals(name)) {
            return HudMode.OFF;
        } else if ("Log".equals(name)) {
            return HudMode.LOG;
        } else if ("Db".equals(name)) {
            return HudMode.DB;
        } else {
            return HudMode.GFX;
        }
    }
}