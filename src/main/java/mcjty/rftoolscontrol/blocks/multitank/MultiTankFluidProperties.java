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

import com.sun.istack.internal.NotNull;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MultiTankFluidProperties {

    @Nonnull private FluidStack contents = FluidStack.EMPTY;
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

    @NotNull
    public FluidStack getContents() {
        return contents.copy();
    }

    @NotNull
    public FluidStack getContentsInternal() {
        return contents;
    }

    public boolean hasContents() {
        return !contents.isEmpty();
    }

    public void drain(int amount) {
        if (contents.isEmpty()) {
            return;
        }
        contents.shrink(amount);
        if (contents.getAmount() <= 0) {
            contents = FluidStack.EMPTY;
        }
        tankTileEntity.markDirty();
    }

    // Warning! Doesn't check if amount fits and is right liquid!
    public void fill(@NotNull FluidStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        if (contents.isEmpty()) {
            contents = stack;
        } else {
            contents.setAmount(contents.getAmount() + stack.getAmount());
        }
        tankTileEntity.markDirty();
    }

    public void set(@NotNull FluidStack stack) {
        if (stack.isEmpty()) {
            contents = FluidStack.EMPTY;
        } else {
            contents = stack.copy();
        }
        tankTileEntity.markDirty();
    }

    public int getCapacity() {
        return capacity;
    }

    public boolean canFill() {
        return canFill;
    }

    public boolean canDrain() {
        return canDrain;
    }

    public boolean canFillFluidType(FluidStack fluidStack) {
        return canFill;
    }

    public boolean canDrainFluidType(FluidStack fluidStack) {
        return canDrain;
    }
}