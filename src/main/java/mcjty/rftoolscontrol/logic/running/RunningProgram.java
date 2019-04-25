package mcjty.rftoolscontrol.logic.running;

import mcjty.rftoolscontrol.api.code.IOpcodeRunnable;
import mcjty.rftoolscontrol.api.machines.IProgram;
import mcjty.rftoolscontrol.api.parameters.Parameter;
import mcjty.rftoolscontrol.api.parameters.ParameterType;
import mcjty.rftoolscontrol.api.parameters.ParameterValue;
import mcjty.rftoolscontrol.blocks.processor.ProcessorTileEntity;
import mcjty.rftoolscontrol.config.ConfigSetup;
import mcjty.rftoolscontrol.logic.TypeConverters;
import mcjty.rftoolscontrol.logic.compiled.CompiledCard;
import mcjty.rftoolscontrol.logic.compiled.CompiledEvent;
import mcjty.rftoolscontrol.logic.compiled.CompiledOpcode;
import mcjty.rftoolscontrol.logic.ParameterTypeTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class RunningProgram implements IProgram {

    public static boolean DEBUG = false;

    // Card we are running from
    private final int cardIndex;

    // Current opcode
    private int current = 0;

    // Event index that caused this program to run
    private int eventIndex = 0;

    // Current ticket
    private String ticket = null;

    // Waiting for a lock
    private String lock = null;

    // If we need to wait a few ticks
    private int delay = 0;

    // We are dead
    private boolean dead = false;

    // Last value result
    private Parameter lastValue;

    // Opcode index, variable index
    private List<FlowStack> loopStack = new ArrayList<>();

    private static class FlowStack {
        private final int current;
        private final Integer var;

        public FlowStack(int current, Integer var) {
            this.current = current;
            this.var = var;
        }

        public int getCurrent() {
            return current;
        }

        public Integer getVar() {
            return var;
        }
    }

    // Cache for the opcodes
    private List<CompiledOpcode> opcodeCache = null;

    public RunningProgram(int cardIndex) {
        this.cardIndex = cardIndex;
    }

    public void startFromEvent(CompiledEvent event) {
        this.current = event.getIndex();
        this.eventIndex = event.getIndex();
    }

    public void setCurrent(int current) {
        this.current = current;
    }

    public int getEventIndex() {
        return eventIndex;
    }

    public void setCraftTicket(@Nullable String craftId) {
        this.ticket = craftId;
    }

    @Override
    @Nullable
    public String getCraftTicket() {
        return ticket;
    }

    @Override
    public boolean hasCraftTicket() {
        return ticket != null && !ticket.isEmpty();
    }

    @Override
    public void setDelay(int delay) {
        this.delay = delay;
    }

    @Override
    public int getDelay() {
        return delay;
    }

    public String getLock() {
        return lock;
    }

    public void setLock(String lock) {
        this.lock = lock;
    }

    @Override
    public void killMe() {
        dead = true;
    }

    @Override
    public boolean isDead() {
        return dead;
    }

    public int getCardIndex() {
        return cardIndex;
    }

    @Override
    public void setLastValue(Parameter value) {
        lastValue = value;
    }

    @Override
    public Parameter getLastValue() {
        return lastValue;
    }

    public CompiledOpcode getCurrentOpcode(ProcessorTileEntity processor) {
        return opcodes(processor).get(current);
    }

    public void pushLoopStack(int varIndex) {
        if (loopStack.size() >= ConfigSetup.maxStackSize.get()) {
            throw new ProgException(ExceptionType.EXCEPT_STACKOVERFLOW);
        }
        loopStack.add(new FlowStack(current, varIndex));
    }

    public void pushCall(int returnIndex) {
        if (loopStack.size() >= ConfigSetup.maxStackSize.get()) {
            throw new ProgException(ExceptionType.EXCEPT_STACKOVERFLOW);
        }

        loopStack.add(new FlowStack(returnIndex, null));
    }

    public void popLoopStack(ProcessorTileEntity processor) {
        if (loopStack.isEmpty()) {
            killMe();
        } else {
            FlowStack pair = loopStack.get(loopStack.size() - 1);
            current = pair.getCurrent();
            Integer varIdx = pair.getVar();
            loopStack.remove(loopStack.size()-1);
            if (varIdx == null) {
                // We are returning from a function call
            } else {
                // We are ending a loop
                Parameter parameter = processor.getVariableArray()[varIdx];
                int i = TypeConverters.convertToInt(parameter);
                i++;
                processor.getVariableArray()[varIdx] = Parameter.builder().type(ParameterType.PAR_INTEGER).value(ParameterValue.constant(i)).build();
            }
        }
    }

    public boolean run(ProcessorTileEntity processor) {
        if (delay > 0) {
            delay--;
            return false;
        }

        if (lock != null) {
            if (processor.testLock(lock)) {
                return false;
            }
            lock = null;
        }

        try {
            CompiledOpcode opcode = opcodes(processor).get(current);
            if (DEBUG) {
                System.out.println(opcode.getOpcode());
            }
            IOpcodeRunnable.OpcodeResult result = opcode.run(processor, this);
            if (result == IOpcodeRunnable.OpcodeResult.POSITIVE) {
                current = opcode.getPrimaryIndex();
            } else if (result == IOpcodeRunnable.OpcodeResult.NEGATIVE){
                current = opcode.getSecondaryIndex();
            } else {
                // Stay at this opcode
            }
        } catch (ProgException e) {
            throw e;
        } catch (Exception e) {
            LogManager.getLogger().log(Level.ERROR, "Opcode failed with: ", e);
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
        tag.setInteger("event", eventIndex);
        tag.setInteger("delay", delay);
        tag.setBoolean("dead", dead);
        if (ticket != null) {
            tag.setString("ticket", ticket);
        }
        if (lock != null) {
            tag.setString("lock", lock);
        }
        if (lastValue != null) {
            NBTTagCompound varTag = new NBTTagCompound();
            varTag.setInteger("type", lastValue.getParameterType().ordinal());
            ParameterTypeTools.writeToNBT(varTag, lastValue.getParameterType(), lastValue.getParameterValue());
            tag.setTag("lastvar", varTag);
        }
        if (!loopStack.isEmpty()) {
            NBTTagList loopList = new NBTTagList();
            for (FlowStack pair : loopStack) {
                NBTTagCompound t = new NBTTagCompound();
                t.setInteger("index", pair.getCurrent());
                t.setInteger("var", pair.getVar() == null ? -1 : pair.getVar());
                loopList.appendTag(t);
            }
            tag.setTag("loopStack", loopList);
        }
    }

    public static RunningProgram readFromNBT(NBTTagCompound tag) {
        if (!tag.hasKey("card")) {
            return null;
        }
        int cardIndex = tag.getInteger("card");
        RunningProgram program = new RunningProgram(cardIndex);
        program.setCurrent(tag.getInteger("current"));
        program.eventIndex = tag.getInteger("event");
        program.setDelay(tag.getInteger("delay"));
        program.dead = tag.getBoolean("dead");
        if (tag.hasKey("ticket")) {
            program.ticket = tag.getString("ticket");
        }
        if (tag.hasKey("lock")) {
            program.lock = tag.getString("lock");
        }
        if (tag.hasKey("lastvar")) {
            NBTTagCompound varTag = tag.getCompoundTag("lastvar");
            int t = varTag.getInteger("type");
            ParameterType type = ParameterType.values()[t];
            program.lastValue = Parameter.builder().type(type).value(ParameterTypeTools.readFromNBT(varTag, type)).build();
        }
        if (tag.hasKey("loopStack")) {
            program.loopStack.clear();
            NBTTagList loopList = tag.getTagList("loopStack", Constants.NBT.TAG_COMPOUND);
            for (int i = 0 ; i < loopList.tagCount() ; i++) {
                NBTTagCompound t = loopList.getCompoundTagAt(i);
                int var = tag.getInteger("var");
                program.loopStack.add(new FlowStack(tag.getInteger("index"), var == -1 ? null : var));
            }
        }
        return program;
    }
}
