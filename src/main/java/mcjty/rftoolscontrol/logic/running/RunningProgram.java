package mcjty.rftoolscontrol.logic.running;

import mcjty.rftoolscontrol.blocks.processor.ProcessorTileEntity;
import mcjty.rftoolscontrol.logic.Parameter;
import mcjty.rftoolscontrol.logic.compiled.CompiledCard;
import mcjty.rftoolscontrol.logic.compiled.CompiledOpcode;
import mcjty.rftoolscontrol.logic.registry.ParameterType;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;
import java.util.List;

public class RunningProgram {

    public static boolean DEBUG = false;

    // Card we are running from
    private final int cardIndex;

    // Current opcode
    private int current = 0;

    // Current craftId
    private String craftId = null;

    // If we need to wait a few ticks
    private int delay = 0;

    // We are dead
    private boolean dead = false;

    // Last value result
    private Parameter lastValue;

    // Cache for the opcodes
    private List<CompiledOpcode> opcodeCache = null;

    public RunningProgram(int cardIndex) {
        this.cardIndex = cardIndex;
    }

    public void setCurrent(int current) {
        this.current = current;
    }

    public void setCraftId(@Nullable String craftId) {
        this.craftId = craftId;
    }

    @Nullable
    public String getCraftId() {
        return craftId;
    }

    public boolean hasCraftId() {
        return craftId != null && !craftId.isEmpty();
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

    public void setLastValue(Parameter value) {
        lastValue = value;
    }

    public Parameter getLastValue() {
        return lastValue;
    }

    public CompiledOpcode getCurrentOpcode(ProcessorTileEntity processor) {
        return opcodes(processor).get(current);
    }

    public boolean run(ProcessorTileEntity processor) {
        if (delay > 0) {
            delay--;
            return false;
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
        } catch (ProgException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ProgException(ExceptionType.EXCEPT_INTERNALERROR);
        }
        return true;
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
        if (lastValue != null) {
            NBTTagCompound varTag = new NBTTagCompound();
            varTag.setInteger("type", lastValue.getParameterType().ordinal());
            lastValue.getParameterType().writeToNBT(varTag, lastValue.getParameterValue());
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
            ParameterType type = ParameterType.values()[t];
            program.lastValue = Parameter.builder().type(type).value(type.readFromNBT(varTag)).build();
        }
        return program;
    }
}
