package mcjty.rftoolscontrol.blocks.multitank;

import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.rftoolscontrol.setup.Registration;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

public class MultiTankContainer extends GenericContainer {

    public static final ContainerFactory CONTAINER_FACTORY = new ContainerFactory(0);

    public MultiTankContainer(int id, ContainerFactory factory, BlockPos pos, @Nullable GenericTileEntity te) {
        super(Registration.MULTITANK_CONTAINER.get(), id, factory, pos, te);
    }
}
