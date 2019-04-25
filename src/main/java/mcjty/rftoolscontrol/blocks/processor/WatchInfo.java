package mcjty.rftoolscontrol.blocks.processor;

public class WatchInfo {
    private final boolean breakOnChange;

    public WatchInfo(boolean breakOnChange) {
        this.breakOnChange = breakOnChange;
    }

    public boolean isBreakOnChange() {
        return breakOnChange;
    }
}
