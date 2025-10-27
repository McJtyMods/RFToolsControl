package mcjty.rftoolscontrol.modules.processor.logic.grid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record GridPos(int x, int y) {

    public static final Codec<GridPos> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("x").forGetter(GridPos::x),
            Codec.INT.fieldOf("y").forGetter(GridPos::y)
    ).apply(instance, GridPos::new));

    public GridPos up() {
        return new GridPos(x, y - 1);
    }

    public GridPos down() {
        return new GridPos(x, y + 1);
    }

    public GridPos left() {
        return new GridPos(x - 1, y);
    }

    public GridPos right() {
        return new GridPos(x + 1, y);
    }

    @Override
    public String toString() {
        return "GridPos{" + x + "," + y + '}';
    }

    public static GridPos pos(int x, int y) {
        return new GridPos(x, y);
    }
}
