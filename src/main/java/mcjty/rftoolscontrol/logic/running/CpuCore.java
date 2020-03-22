package mcjty.rftoolscontrol.logic.running;

import mcjty.rftoolscontrol.blocks.processor.ProcessorTileEntity;
import mcjty.rftoolscontrol.config.ConfigSetup;
import net.minecraft.nbt.CompoundNBT;

public class CpuCore {

    private RunningProgram program = null;
    private int tier;
    private boolean debug = false;

    public CpuCore() {
    }

    public void setTier(int tier) {
        this.tier = tier;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public void run(ProcessorTileEntity processor) {
        if (debug) {
            return;
        }
        for (int i = 0; i < ConfigSetup.coreSpeed[tier].get(); i++) {
            boolean rc = false;
            try {
                rc = program.run(processor);
            } catch (ProgException e) {
                processor.exception(e.getExceptionType(), program);
                program.killMe();
            }
            if (program.isDead()) {
                stopProgram(processor);
                return;
            }
            if (!rc) {
                return;
            }
        }
    }

    public void step(ProcessorTileEntity processor, CpuCore core) {
        try {
            program.run(processor);
        } catch (ProgException e) {
            processor.exception(e.getExceptionType(), program);
            program.killMe();
        }
        if (program.isDead()) {
            stopProgram(processor);
        }
    }

    public int getTier() {
        return tier;
    }

    public boolean hasProgram() {
        return program != null;
    }

    public RunningProgram getProgram() {
        return program;
    }

    public void stopProgram() {
        program = null;
    }

    private void stopProgram(ProcessorTileEntity processor) {
        if (RunningProgram.DEBUG) {
            System.out.println("Core: stopping program");
        }
        processor.clearRunningEvent(program.getCardIndex(), program.getEventIndex());
        program = null;
    }

    public void startProgram(RunningProgram program) {
        if (RunningProgram.DEBUG) {
            System.out.println("Core: starting program = " + program);
        }
        this.program = program;
        program.setCore(this);
    }

    public CompoundNBT writeToNBT() {
        CompoundNBT tag = new CompoundNBT();
        if (program != null) {
            program.writeToNBT(tag);
        }
        tag.setInteger("tier", tier);
        tag.setBoolean("debug", debug);
        return tag;
    }

    public void readFromNBT(CompoundNBT tag) {
        program = RunningProgram.readFromNBT(tag);
        if (program != null) {
            program.setCore(this);
        }
        tier = tag.getInteger("tier");
        debug = tag.getBoolean("debug");
    }
}
