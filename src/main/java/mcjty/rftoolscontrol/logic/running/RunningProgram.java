package mcjty.rftoolscontrol.logic.running;

import mcjty.rftoolscontrol.blocks.processor.CardInfo;
import mcjty.rftoolscontrol.blocks.processor.ProcessorTileEntity;
import mcjty.rftoolscontrol.logic.compiled.CompiledCard;
import mcjty.rftoolscontrol.logic.compiled.CompiledOpcode;
import net.minecraft.nbt.NBTTagCompound;

import java.util.List;

public class RunningProgram {

    // Card we are running from
    private final int cardIndex;

    // Current opcode
    private int current = 0;

    // If we need to wait a few ticks
    private int delay = 0;

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

    public void run(ProcessorTileEntity processor) {
        if (delay > 0) {
            delay--;
            return;
        }

        CompiledOpcode opcode = opcodes(processor).get(current);
        if (opcode.run(processor, this)) {
            current = opcode.getPrimaryIndex();
        } else {
            current = opcode.getSecondaryIndex();
        }
    }

    private List<CompiledOpcode> opcodes(ProcessorTileEntity processor) {
        if (opcodeCache == null) {
            CardInfo info = processor.getCardInfo(cardIndex);
            CompiledCard card = info.getCompiledCard();
            opcodeCache = card.getOpcodes();
        }
        return opcodeCache;
    }

    public void writeToNBT(NBTTagCompound tag) {
        tag.setInteger("card", cardIndex);
        tag.setInteger("current", current);
        tag.setInteger("delay", delay);
    }

    public static RunningProgram readFromNBT(NBTTagCompound tag) {
        if (!tag.hasKey("card")) {
            return null;
        }
        int cardIndex = tag.getInteger("card");
        RunningProgram program = new RunningProgram(cardIndex);
        program.setCurrent(tag.getInteger("current"));
        program.setDelay(tag.getInteger("delay"));
        return program;
    }
}
