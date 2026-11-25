package mcjty.rftoolscontrol.modules.multitank.util;

import mcjty.lib.container.IndexedStorage;
import mcjty.rftoolscontrol.modules.multitank.blocks.MultiTankTileEntity;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public class MultiTankHandler implements IndexedStorage<FluidVariant> {

    private final MultiTankTileEntity tank;

    public MultiTankHandler(MultiTankTileEntity tank) {
        this.tank = tank;
    }

    @Override
    public int size() {
        return MultiTankTileEntity.TANKS;
    }

    @Override
    public FluidVariant getResource(int index) {
        return tank.getProperties()[index].getContentsInternal().variant();
    }

    @Override
    public long getAmountAsLong(int index) {
        return tank.getProperties()[index].getContentsInternal().getAmount() * (FluidConstants.BUCKET / 1000);
    }

    @Override
    public long getCapacityAsLong(int index, FluidVariant resource) {
        return tank.getProperties()[index].getCapacity() * (FluidConstants.BUCKET / 1000);
    }

    @Override
    public boolean isValid(int index, FluidVariant resource) {
        return false;
    }

    @Override
    public boolean supportsInsertion() {
        return false;
    }

    @Override
    public boolean supportsExtraction() {
        return false;
    }

    @Override
    public long insert(int index, FluidVariant resource, long amount, TransactionContext transaction) {
        return 0;
    }

    @Override
    public long extract(int index, FluidVariant resource, long amount, TransactionContext transaction) {
        return 0;
    }
}
