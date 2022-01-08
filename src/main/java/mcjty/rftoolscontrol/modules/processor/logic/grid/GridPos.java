package mcjty.rftoolscontrol.modules.processor.logic.grid;

public record GridPos(int x, int y) {

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
