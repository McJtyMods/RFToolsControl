/*
 * Minecraft Forge
 * Copyright (c) 2016.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package mcjty.rftoolscontrol.blocks.multitank;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MultiTankFluidProperties implements IFluidTankProperties {

    @Nullable private FluidStack contents;
    private final int capacity;
    private final boolean canFill;
    private final boolean canDrain;

    @Nonnull private final MultiTankTileEntity tankTileEntity;

    public MultiTankFluidProperties(@Nonnull MultiTankTileEntity tankTileEntity, @Nullable FluidStack contents, int capacity) {
        this(tankTileEntity, contents, capacity, true, true);
    }

    public MultiTankFluidProperties(@Nonnull MultiTankTileEntity tankTileEntity, @Nullable FluidStack contents, int capacity, boolean canFill, boolean canDrain) {
        this.tankTileEntity = tankTileEntity;
        this.contents = contents;
        this.capacity = capacity;
        this.canFill = canFill;
        this.canDrain = canDrain;
    }

    @Nullable
    @Override
    public FluidStack getContents() {
        return contents == null ? null : contents.copy();
    }

    public FluidStack getContentsInternal() {
        return contents;
    }

    public boolean hasContents() {
        return contents != null;
    }

    public void drain(int amount) {
        if (contents == null) {
            return;
        }
        contents.amount -= amount;
        if (contents.amount <= 0) {
            contents = null;
        }
        tankTileEntity.markDirty();
    }

    // Warning! Doesn't check if amount fits and is right liquid!
    public void fill(FluidStack stack) {
        if (stack == null) {
            return;
        }
        if (contents == null) {
            contents = stack;
        } else {
            contents.amount += stack.amount;
        }
        tankTileEntity.markDirty();
    }

    public void set(FluidStack stack) {
        if (stack == null) {
            contents = null;
        } else {
            contents = stack.copy();
        }
        tankTileEntity.markDirty();
    }

    @Override
    public int getCapacity() {
        return capacity;
    }

    @Override
    public boolean canFill() {
        return canFill;
    }

    @Override
    public boolean canDrain() {
        return canDrain;
    }

    @Override
    public boolean canFillFluidType(FluidStack fluidStack) {
        return canFill;
    }

    @Override
    public boolean canDrainFluidType(FluidStack fluidStack) {
        return canDrain;
    }
}