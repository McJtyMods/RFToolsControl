package mcjty.rftoolscontrol.blocks.multitank;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;

class MultiTankHandler implements IFluidHandler {

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
