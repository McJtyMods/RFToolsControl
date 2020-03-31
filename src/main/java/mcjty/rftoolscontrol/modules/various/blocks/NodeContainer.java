package mcjty.rftoolscontrol.modules.various.blocks;

import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.rftoolscontrol.modules.various.VariousSetup;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

public class NodeContainer extends GenericContainer {

    public static final ContainerFactory CONTAINER_FACTORY = new ContainerFactory(0);

    public NodeContainer(int id, ContainerFactory factory, BlockPos pos, @Nullable GenericTileEntity te) {
        super(VariousSetup.NODE_CONTAINER.get(), id, factory, pos, te);
    }
}
