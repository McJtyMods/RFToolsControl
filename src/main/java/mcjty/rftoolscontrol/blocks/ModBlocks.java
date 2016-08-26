package mcjty.rftoolscontrol.blocks;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class ModBlocks {

    public static ProgrammerBlock programmerBlock;

    public static void init() {
        programmerBlock = new ProgrammerBlock();
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        programmerBlock.initModel();
    }
}
