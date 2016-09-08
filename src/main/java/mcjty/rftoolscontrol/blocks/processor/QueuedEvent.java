package mcjty.rftoolscontrol.blocks.processor;

import mcjty.rftoolscontrol.logic.compiled.CompiledEvent;

public class QueuedEvent {
    private final int cardIndex;
    private final CompiledEvent compiledEvent;
    private final String craftId;

    public QueuedEvent(int cardIndex, CompiledEvent compiledEvent, String craftId) {
        this.cardIndex = cardIndex;
        this.compiledEvent = compiledEvent;
        this.craftId = craftId;
    }

    public int getCardIndex() {
        return cardIndex;
    }

    public CompiledEvent getCompiledEvent() {
        return compiledEvent;
    }

    public String getCraftId() {
        return craftId;
    }
}
