package mcjty.rftoolscontrol.logic.running;

import mcjty.rftoolscontrol.blocks.processor.ProcessorTileEntity;
import net.minecraft.nbt.NBTTagCompound;

public class CpuCore {

    private RunningProgram program = null;

    public void run(ProcessorTileEntity processor) {
        if (program != null) {
            program.run(processor);
        }
    }

    public boolean hasProgram() {
        return program != null;
    }

    public void startProgram(RunningProgram program) {
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
