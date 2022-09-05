package mcjty.rftoolscontrol.modules.processor.util;

import mcjty.rftoolsbase.api.control.parameters.Parameter;
import mcjty.rftoolscontrol.modules.processor.logic.compiled.CompiledEvent;

import javax.annotation.Nullable;

public record QueuedEvent(int cardIndex,
                          CompiledEvent compiledEvent,
                          @Nullable String ticket,
                          @Nullable Parameter parameter) {
    public QueuedEvent(int cardIndex, CompiledEvent compiledEvent, @Nullable String ticket, @Nullable Parameter parameter) {
        this.cardIndex = cardIndex;
        this.compiledEvent = compiledEvent;
        this.ticket = ticket;
        this.parameter = parameter;
    }
}
