package mcjty.rftoolscontrol.modules.multitank.util;

import mcjty.rftoolscontrol.modules.multitank.blocks.MultiTankTileEntity;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;

import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

public class MultiTankHandler implements IFluidHandler {

    private final MultiTankTileEntity tank;

    public MultiTankHandler(MultiTankTileEntity tank) {
        this.tank = tank;
    }

    @Override
    public int getTanks() {
        return MultiTankTileEntity.TANKS;
    }

    @Nonnull
    @Override
    public FluidStack getFluidInTank(int id) {
        return tank.getProperties()[id].getContents();
    }

    @Override
    public int getTankCapacity(int id) {
        return tank.getProperties()[id].getCapacity();
    }

    @Override
    public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
        return false;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return 0;
    }

    @Nonnull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        return FluidStack.EMPTY;
    }

    @Nonnull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        return FluidStack.EMPTY;
    }
}
