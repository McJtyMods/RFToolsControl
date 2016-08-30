package mcjty.rftoolscontrol.blocks;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class ModBlocks {

    public static ProgrammerBlock programmerBlock;
    public static ProcessorBlock processorBlock;

    public static void init() {
        programmerBlock = new ProgrammerBlock();
        processorBlock = new ProcessorBlock();
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        programmerBlock.initModel();
        processorBlock.initModel();
    }
}
