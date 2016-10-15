package mcjty.rftoolscontrol.blocks;

import mcjty.rftoolscontrol.blocks.craftingstation.CraftingStationBlock;
import mcjty.rftoolscontrol.blocks.multitank.MultiTankBlock;
import mcjty.rftoolscontrol.blocks.node.NodeBlock;
import mcjty.rftoolscontrol.blocks.processor.ProcessorBlock;
import mcjty.rftoolscontrol.blocks.programmer.ProgrammerBlock;
import mcjty.rftoolscontrol.blocks.workbench.WorkbenchBlock;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class ModBlocks {

    public static ProgrammerBlock programmerBlock;
    public static ProcessorBlock processorBlock;
    public static NodeBlock nodeBlock;
    public static CraftingStationBlock craftingStationBlock;
    public static WorkbenchBlock workbenchBlock;
    public static MultiTankBlock multiTankBlock;

    public static void init() {
        programmerBlock = new ProgrammerBlock();
        processorBlock = new ProcessorBlock();
        nodeBlock = new NodeBlock();
        craftingStationBlock = new CraftingStationBlock();
        workbenchBlock = new WorkbenchBlock();
        multiTankBlock = new MultiTankBlock();
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        programmerBlock.initModel();
        processorBlock.initModel();
        nodeBlock.initModel();
        craftingStationBlock.initModel();
        workbenchBlock.initModel();
        multiTankBlock.initModel();
    }
}
