package mcjty.rftoolscontrol.blocks;

import mcjty.rftoolscontrol.blocks.node.NodeBlock;
import mcjty.rftoolscontrol.blocks.processor.ProcessorBlock;
import mcjty.rftoolscontrol.blocks.programmer.ProgrammerBlock;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class ModBlocks {

    public static ProgrammerBlock programmerBlock;
    public static ProcessorBlock processorBlock;
    public static NodeBlock nodeBlock;

    public static void init() {
        programmerBlock = new ProgrammerBlock();
        processorBlock = new ProcessorBlock();
        nodeBlock = new NodeBlock();
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        programmerBlock.initModel();
        processorBlock.initModel();
        nodeBlock.initModel();
    }
}
