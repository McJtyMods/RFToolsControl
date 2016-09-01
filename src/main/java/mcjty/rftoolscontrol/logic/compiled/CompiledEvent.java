package mcjty.rftoolscontrol.logic.compiled;

public class CompiledEvent {

    // Index for the opcode to start this event
    private final int index;

    public CompiledEvent(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}
