package mcjty.rftoolscontrol.blocks.node;

import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.rftoolscontrol.setup.Registration;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

public class NodeContainer extends GenericContainer {

    public static final ContainerFactory CONTAINER_FACTORY = new ContainerFactory(0);

    public NodeContainer(int id, ContainerFactory factory, BlockPos pos, @Nullable GenericTileEntity te) {
        super(Registration.NODE_CONTAINER.get(), id, factory, pos, te);
    }
}
