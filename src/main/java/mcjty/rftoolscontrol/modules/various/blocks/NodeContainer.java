package mcjty.rftoolscontrol.modules.various.blocks;

import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.rftoolscontrol.modules.various.VariousModule;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Lazy;

import javax.annotation.Nullable;

public class NodeContainer extends GenericContainer {

    public static final Lazy<ContainerFactory> CONTAINER_FACTORY = Lazy.of(() -> new ContainerFactory(0));

    public NodeContainer(int id, ContainerFactory factory, BlockPos pos, @Nullable GenericTileEntity te) {
        super(VariousModule.NODE_CONTAINER.get(), id, factory, pos, te);
    }
}
