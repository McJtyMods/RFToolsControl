package mcjty.rftoolscontrol.modules.processor.util;

import mcjty.rftoolscontrol.modules.processor.logic.Parameter;
import mcjty.rftoolscontrol.modules.processor.logic.compiled.CompiledEvent;

import javax.annotation.Nullable;

public class QueuedEvent {
    private final int cardIndex;
    private final CompiledEvent compiledEvent;

    @Nullable private final String ticket;
    @Nullable private final Parameter parameter;

    public QueuedEvent(int cardIndex, CompiledEvent compiledEvent, @Nullable String ticket, @Nullable Parameter parameter) {
        this.cardIndex = cardIndex;
        this.compiledEvent = compiledEvent;
        this.ticket = ticket;
        this.parameter = parameter;
    }

    public int getCardIndex() {
        return cardIndex;
    }

    public CompiledEvent getCompiledEvent() {
        return compiledEvent;
    }

    @Nullable
    public String getTicket() {
        return ticket;
    }

    @Nullable
    public Parameter getParameter() {
        return parameter;
    }
}
