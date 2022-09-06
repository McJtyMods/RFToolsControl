package mcjty.rftoolscontrol.modules.processor.logic.running;

import mcjty.rftoolsbase.api.control.code.IOpcodeRunnable;
import mcjty.rftoolsbase.api.control.machines.IProgram;
import mcjty.rftoolsbase.api.control.parameters.IParameter;
import mcjty.rftoolsbase.api.control.parameters.Parameter;
import mcjty.rftoolsbase.api.control.parameters.ParameterType;
import mcjty.rftoolsbase.api.control.parameters.ParameterValue;
import mcjty.rftoolscontrol.modules.processor.blocks.ProcessorTileEntity;
import mcjty.rftoolscontrol.modules.processor.logic.ParameterTypeTools;
import mcjty.rftoolscontrol.modules.processor.logic.TypeConverters;
import mcjty.rftoolscontrol.modules.processor.logic.compiled.CompiledCard;
import mcjty.rftoolscontrol.modules.processor.logic.compiled.CompiledEvent;
import mcjty.rftoolscontrol.modules.processor.logic.compiled.CompiledOpcode;
import mcjty.rftoolscontrol.setup.Config;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
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
    private IParameter lastValue;

    // The core we are running on
    private CpuCore core;

    // Opcode index, variable index
    private final List<FlowStack> loopStack = new ArrayList<>();

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
        this.current = event.index();
        this.eventIndex = event.index();
    }

    public void setCurrent(int current) {
        this.current = current;
    }

    public void setCore(CpuCore core) {
        this.core = core;
    }

    public CpuCore getCore() {
        return core;
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
    public void setLastValue(IParameter value) {
        lastValue = value;
    }

    @Override
    public IParameter getLastValue() {
        return lastValue;
    }

    public CompiledOpcode getCurrentOpcode(ProcessorTileEntity processor) {
        return opcodes(processor).get(current);
    }

    public void pushLoopStack(int varIndex) {
        if (loopStack.size() >= Config.maxStackSize.get()) {
            throw new ProgException(ExceptionType.EXCEPT_STACKOVERFLOW);
        }
        loopStack.add(new FlowStack(current, varIndex));
    }

    public void pushCall(int returnIndex) {
        if (loopStack.size() >= Config.maxStackSize.get()) {
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
                processor.setVariableInternal(this, varIdx, Parameter.builder().type(ParameterType.PAR_INTEGER).value(ParameterValue.constant(i)).build());
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

    public void writeToNBT(CompoundTag tag) {
        tag.putInt("card", cardIndex);
        tag.putInt("current", current);
        tag.putInt("event", eventIndex);
        tag.putInt("delay", delay);
        tag.putBoolean("dead", dead);
        if (ticket != null) {
            tag.putString("ticket", ticket);
        }
        if (lock != null) {
            tag.putString("lock", lock);
        }
        if (lastValue != null) {
            CompoundTag varTag = new CompoundTag();
            varTag.putInt("type", lastValue.getParameterType().ordinal());
            ParameterTypeTools.writeToNBT(varTag, lastValue.getParameterType(), lastValue.getParameterValue());
            tag.put("lastvar", varTag);
        }
        if (!loopStack.isEmpty()) {
            ListTag loopList = new ListTag();
            for (FlowStack pair : loopStack) {
                CompoundTag t = new CompoundTag();
                t.putInt("index", pair.getCurrent());
                t.putInt("var", pair.getVar() == null ? -1 : pair.getVar());
                loopList.add(t);
            }
            tag.put("loopStack", loopList);
        }
    }

    public static RunningProgram readFromNBT(CompoundTag tag) {
        if (!tag.contains("card")) {
            return null;
        }
        int cardIndex = tag.getInt("card");
        RunningProgram program = new RunningProgram(cardIndex);
        program.setCurrent(tag.getInt("current"));
        program.eventIndex = tag.getInt("event");
        program.setDelay(tag.getInt("delay"));
        program.dead = tag.getBoolean("dead");
        if (tag.contains("ticket")) {
            program.ticket = tag.getString("ticket");
        }
        if (tag.contains("lock")) {
            program.lock = tag.getString("lock");
        }
        if (tag.contains("lastvar")) {
            CompoundTag varTag = tag.getCompound("lastvar");
            int t = varTag.getInt("type");
            ParameterType type = ParameterType.values()[t];
            program.lastValue = Parameter.builder().type(type).value(ParameterTypeTools.readFromNBT(varTag, type)).build();
        }
        if (tag.contains("loopStack")) {
            program.loopStack.clear();
            ListTag loopList = tag.getList("loopStack", Tag.TAG_COMPOUND);
            for (int i = 0 ; i < loopList.size() ; i++) {
                CompoundTag t = loopList.getCompound(i);
                int var = tag.getInt("var");
                program.loopStack.add(new FlowStack(tag.getInt("index"), var == -1 ? null : var));
            }
        }
        return program;
    }
}
