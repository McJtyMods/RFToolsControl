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
}
