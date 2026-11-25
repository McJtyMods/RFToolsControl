package mcjty.rftoolscontrol.modules.multitank.util;

import mcjty.rftoolscontrol.modules.multitank.blocks.MultiTankTileEntity;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class MultiTankHandler implements ResourceHandler<FluidResource> {

    private final MultiTankTileEntity tank;

    public MultiTankHandler(MultiTankTileEntity tank) {
        this.tank = tank;
    }

    @Override
    public int size() {
        return MultiTankTileEntity.TANKS;
    }

    @Override
    public FluidResource getResource(int index) {
        return FluidResource.of(tank.getProperties()[index].getContentsInternal());
    }

    @Override
    public long getAmountAsLong(int index) {
        return tank.getProperties()[index].getContentsInternal().getAmount();
    }

    @Override
    public long getCapacityAsLong(int index, FluidResource resource) {
        return tank.getProperties()[index].getCapacity();
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
        return false;
    }

    @Override
    public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
        return 0;
    }

    @Override
    public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
        return 0;
    }
}
