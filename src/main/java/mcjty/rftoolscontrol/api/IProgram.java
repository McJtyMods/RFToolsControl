package mcjty.rftoolscontrol.api;

import mcjty.rftoolscontrol.logic.Parameter;

import javax.annotation.Nullable;

/**
 * A representation of a program
 */
public interface IProgram {
    void setLastValue(Parameter value);

    Parameter getLastValue();

    @Nullable
    String getCraftTicket();

    public boolean hasCraftTicket();

    void setDelay(int delay);

    int getDelay();

    void killMe();

    boolean isDead();
}
