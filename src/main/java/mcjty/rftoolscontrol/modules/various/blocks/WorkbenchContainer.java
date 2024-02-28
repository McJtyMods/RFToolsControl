package mcjty.rftoolscontrol.modules.various.blocks;

import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.container.SlotDefinition;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.rftoolscontrol.modules.various.VariousModule;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.common.util.Lazy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static mcjty.lib.container.SlotDefinition.generic;

public class WorkbenchContainer extends GenericContainer {

    public WorkbenchContainer(int id, ContainerFactory factory, BlockPos pos, @Nullable GenericTileEntity te, @Nonnull Player player) {
        super(VariousModule.WORKBENCH_CONTAINER.get(), id, factory, pos, te, player);
    }
}
