package mcjty.rftoolscontrol.api.parameters;

public class Tuple implements Comparable<Tuple> {
    private final int x;
    private final int y;

    public Tuple(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public int compareTo(Tuple tuple) {
        if (x < tuple.x) {
            return -1;
        } else if (x > tuple.x) {
            return 1;
        } else {
            if (y < tuple.y) {
                return -1;
            } else if (y > tuple.y) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Tuple tuple = (Tuple) o;

        if (x != tuple.x) {
            return false;
        }
        if (y != tuple.y) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        return result;
    }

    @Override
    public String toString() {
        return x + "," + y;
    }
}
