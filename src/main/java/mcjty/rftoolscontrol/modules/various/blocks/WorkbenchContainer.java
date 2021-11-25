package mcjty.rftoolscontrol.modules.various.blocks;

import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.container.SlotDefinition;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.rftoolscontrol.modules.various.VariousModule;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Lazy;

import javax.annotation.Nullable;

import static mcjty.lib.container.SlotDefinition.generic;

public class WorkbenchContainer extends GenericContainer {

    public WorkbenchContainer(int id, ContainerFactory factory, BlockPos pos, @Nullable GenericTileEntity te) {
        super(VariousModule.WORKBENCH_CONTAINER.get(), id, factory, pos, te);
    }
}
