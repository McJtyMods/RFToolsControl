package mcjty.rftoolscontrol.logic.running;

import mcjty.rftoolscontrol.blocks.processor.ProcessorTileEntity;
import mcjty.rftoolscontrol.config.GeneralConfiguration;
import net.minecraft.nbt.NBTTagCompound;

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
        for (int i = 0; i < GeneralConfiguration.coreSpeed[tier]; i++) {
            boolean rc = false;
            try {
                rc = program.run(processor);
            } catch (ProgException e) {
                processor.exception(e.getExceptionType(), program);
                program.killMe();
            }
            if (program.isDead()) {
                if (RunningProgram.DEBUG) {
                    System.out.println("Core: stopping program");
                }
                program = null;
                return;
            }
            if (!rc) {
                return;
            }
        }
    }

    public void step(ProcessorTileEntity processor) {
        try {
            program.run(processor);
        } catch (ProgException e) {
            processor.exception(e.getExceptionType(), program);
            program.killMe();
        }
        if (program.isDead()) {
            if (RunningProgram.DEBUG) {
                System.out.println("Core: stopping program");
            }
            program = null;
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

    public void startProgram(RunningProgram program) {
        if (RunningProgram.DEBUG) {
            System.out.println("Core: starting program = " + program);
        }
        this.program = program;
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        if (program != null) {
            program.writeToNBT(tag);
        }
        tag.setInteger("tier", tier);
        tag.setBoolean("debug", debug);
        return tag;
    }

    public void readFromNBT(NBTTagCompound tag) {
        program = RunningProgram.readFromNBT(tag);
        tier = tag.getInteger("tier");
        debug = tag.getBoolean("debug");
    }
}
