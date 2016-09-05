package mcjty.rftoolscontrol.logic.running;

import mcjty.rftoolscontrol.blocks.processor.ProcessorTileEntity;
import mcjty.rftoolscontrol.config.GeneralConfiguration;
import net.minecraft.nbt.NBTTagCompound;

public class CpuCore {

    private RunningProgram program = null;
    private int tier;

    public CpuCore() {
    }

    public void setTier(int tier) {
        this.tier = tier;
    }

    public void run(ProcessorTileEntity processor) {
        for (int i = 0; i < GeneralConfiguration.coreSpeed[tier]; i++) {
            program.run(processor);
            if (program.isDead()) {
                System.out.println("Core: stopping program");
                program = null;
                return;
            }
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
        System.out.println("Core: starting program = " + program);
        this.program = program;
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        if (program != null) {
            program.writeToNBT(tag);
        }
        tag.setInteger("tier", tier);
        return tag;
    }

    public void readFromNBT(NBTTagCompound tag) {
        program = RunningProgram.readFromNBT(tag);
        tier = tag.getInteger("tier");
    }
}
