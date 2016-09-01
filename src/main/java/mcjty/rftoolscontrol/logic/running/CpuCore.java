package mcjty.rftoolscontrol.logic.running;

import mcjty.rftoolscontrol.blocks.processor.ProcessorTileEntity;
import net.minecraft.nbt.NBTTagCompound;

public class CpuCore {

    private RunningProgram program = null;

    public void run(ProcessorTileEntity processor) {
        if (program != null) {
            program.run(processor);
            if (program.isDead()) {
                System.out.println("Core: stopping program");
                program = null;
            }
        }
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
        return tag;
    }

    public void readFromNBT(NBTTagCompound tag) {
        program = RunningProgram.readFromNBT(tag);
    }
}
