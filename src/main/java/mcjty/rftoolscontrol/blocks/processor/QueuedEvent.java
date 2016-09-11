package mcjty.rftoolscontrol.blocks.processor;

import mcjty.rftoolscontrol.logic.compiled.CompiledEvent;

public class QueuedEvent {
    private final int cardIndex;
    private final CompiledEvent compiledEvent;
    private final String ticket;

    public QueuedEvent(int cardIndex, CompiledEvent compiledEvent, String ticket) {
        this.cardIndex = cardIndex;
        this.compiledEvent = compiledEvent;
        this.ticket = ticket;
    }

    public int getCardIndex() {
        return cardIndex;
    }

    public CompiledEvent getCompiledEvent() {
        return compiledEvent;
    }

    public String getTicket() {
        return ticket;
    }
}
