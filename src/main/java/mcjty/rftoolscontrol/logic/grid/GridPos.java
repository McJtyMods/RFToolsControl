package mcjty.rftoolscontrol.logic.grid;

public class GridPos {

    private final int x;
    private final int y;

    public GridPos(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public GridPos up() {
        return new GridPos(x, y-1);
    }
    public GridPos down() {
        return new GridPos(x, y+1);
    }
    public GridPos left() {
        return new GridPos(x-1, y);
    }
    public GridPos right() {
        return new GridPos(x+1, y);
    }

    @Override
    public String toString() {
        return "GridPos{" + x + "," + y + '}';
    }

    public static GridPos pos(int x, int y) {
        return new GridPos(x, y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GridPos gridPos = (GridPos) o;

        if (x != gridPos.x) return false;
        return y == gridPos.y;

    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        return result;
    }
}
