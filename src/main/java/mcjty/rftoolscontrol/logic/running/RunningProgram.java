package mcjty.rftoolscontrol.logic.running;

import mcjty.rftoolscontrol.blocks.processor.ProcessorTileEntity;
import mcjty.rftoolscontrol.logic.compiled.CompiledCard;
import mcjty.rftoolscontrol.logic.compiled.CompiledOpcode;
import mcjty.rftoolscontrol.logic.registry.ParameterType;
import mcjty.rftoolscontrol.logic.registry.ParameterValue;
import net.minecraft.nbt.NBTTagCompound;

import java.util.List;

public class RunningProgram {

    private static boolean DEBUG = true;

    // Card we are running from
    private final int cardIndex;

    // Current opcode
    private int current = 0;

    // If we need to wait a few ticks
    private int delay = 0;

    // We are dead
    private boolean dead = false;

    // Last value result
    private ParameterType lastValueType;
    private ParameterValue lastValue;

    // Cache for the opcodes
    private List<CompiledOpcode> opcodeCache = null;

    public RunningProgram(int cardIndex) {
        this.cardIndex = cardIndex;
    }

    public void setCurrent(int current) {
        this.current = current;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public void killMe() {
        dead = true;
    }

    public boolean isDead() {
        return dead;
    }

    public int getCardIndex() {
        return cardIndex;
    }

    public void setLastValue(ParameterType type, ParameterValue value) {
        lastValueType = type;
        lastValue = value;
    }

    public ParameterType getLastValueType() {
        return lastValueType;
    }

    // This is always a constant value
    public ParameterValue getLastValue() {
        return lastValue;
    }

    public void run(ProcessorTileEntity processor) {
        if (delay > 0) {
            delay--;
            return;
        }

        try {
            CompiledOpcode opcode = opcodes(processor).get(current);
            if (DEBUG) {
                System.out.println(opcode.getOpcode());
            }
            if (opcode.run(processor, this)) {
                current = opcode.getPrimaryIndex();
            } else {
                current = opcode.getSecondaryIndex();
            }
        } catch (Exception e) {
            processor.log("[ERROR]");
            e.printStackTrace();
            killMe();
        }
    }

    private List<CompiledOpcode> opcodes(ProcessorTileEntity processor) {
        if (opcodeCache == null) {
            CompiledCard card = processor.getCompiledCard(cardIndex);
            opcodeCache = card.getOpcodes();
        }
        return opcodeCache;
    }

    public void writeToNBT(NBTTagCompound tag) {
        tag.setInteger("card", cardIndex);
        tag.setInteger("current", current);
        tag.setInteger("delay", delay);
        tag.setBoolean("dead", dead);
        if (lastValueType != null && lastValue != null) {
            NBTTagCompound varTag = new NBTTagCompound();
            varTag.setInteger("type", lastValueType.ordinal());
            lastValueType.writeToNBT(varTag, lastValue);
            tag.setTag("lastvar", varTag);
        }
    }

    public static RunningProgram readFromNBT(NBTTagCompound tag) {
        if (!tag.hasKey("card")) {
            return null;
        }
        int cardIndex = tag.getInteger("card");
        RunningProgram program = new RunningProgram(cardIndex);
        program.setCurrent(tag.getInteger("current"));
        program.setDelay(tag.getInteger("delay"));
        program.dead = tag.getBoolean("dead");
        if (tag.hasKey("lastvar")) {
            NBTTagCompound varTag = tag.getCompoundTag("lastvar");
            int t = varTag.getInteger("type");
            program.lastValueType = ParameterType.values()[t];
            program.lastValue = program.lastValueType.readFromNBT(varTag);
        }
        return program;
    }
}
