package mcjty.rftoolscontrol.logic.compiled;

public class CompiledEvent {

    // Index for the opcode to start this event
    private final int index;

    // If we have a single-run event
    private final boolean single;

    public CompiledEvent(int index, boolean single) {
        this.index = index;
        this.single = single;
    }

    public int getIndex() {
        return index;
    }

    public boolean isSingle() {
        return single;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CompiledEvent that = (CompiledEvent) o;

        if (index != that.index) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return index;
    }
}
