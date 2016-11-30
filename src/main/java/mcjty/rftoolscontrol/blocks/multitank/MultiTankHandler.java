package mcjty.rftoolscontrol.blocks.multitank;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;

class MultiTankHandler implements IFluidHandler {

    private final MultiTankTileEntity tank;

    public MultiTankHandler(MultiTankTileEntity tank) {
        this.tank = tank;
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        return tank.getProperties();
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        return 0;
    }

    @Nullable
    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        return null;
    }

    @Nullable
    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        return null;
    }
}
