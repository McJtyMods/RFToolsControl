package mcjty.rftoolscontrol.blocks.multitank;

import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.typed.Type;
import mcjty.lib.typed.TypedMap;
import mcjty.rftoolscontrol.setup.Registration;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MultiTankTileEntity extends GenericTileEntity {

    public static final String CMD_GETFLUIDS = "getFluids";
    public static final String CLIENTCMD_GETFLUIDS = "getFluids";

    public static final int TANKS = 4;
    public static final int MAXCAPACITY = 10000;

    private final MultiTankFluidProperties properties[] = new MultiTankFluidProperties[TANKS];
    private final FluidStack fluids[] = new FluidStack[TANKS];

    private LazyOptional<MultiTankHandler> fluidHandler = LazyOptional.of(this::createFluidHandler);

    public MultiTankTileEntity() {
        super(Registration.MULTITANK_TILE.get());
        for (int i = 0 ; i < TANKS ; i++) {
            properties[i] = new MultiTankFluidProperties(this, null, MAXCAPACITY);
        }
    }

    public MultiTankFluidProperties[] getProperties() {
        return properties;
    }

    @Override
    public void read(CompoundNBT tagCompound) {
        super.read(tagCompound);
        readRestorableFromNBT(tagCompound);
    }

    @Override
    public CompoundNBT write(CompoundNBT tagCompound) {
        super.write(tagCompound);
        writeRestorableToNBT(tagCompound);
        return tagCompound;
    }

    // @todo 1.15 loot tables
    public void readRestorableFromNBT(CompoundNBT tagCompound) {
        for (int i = 0 ; i < TANKS ; i++) {
            properties[i] = new MultiTankFluidProperties(this, FluidStack.loadFluidStackFromNBT(tagCompound.getCompound("f" + i)), MAXCAPACITY);
        }
    }

    // @todo 1.15 loot tables
    public void writeRestorableToNBT(CompoundNBT tagCompound) {
        for (int i = 0 ; i < TANKS ; i++) {
            FluidStack contents = properties[i].getContents();
            if (contents != null) {
                CompoundNBT tag = new CompoundNBT();
                contents.writeToNBT(tag);
                tagCompound.put("f" + i, tag);
            }
        }
    }

    @Nonnull
    @Override
    public <T> List<T> executeWithResultList(String command, TypedMap args, Type<T> type) {
        List<T> rc = super.executeWithResultList(command, args, type);
        if (!rc.isEmpty()) {
            return rc;
        }
        if (CMD_GETFLUIDS.equals(command)) {
            List<FluidStack> result = new ArrayList<>(TANKS);
            for (MultiTankFluidProperties property : properties) {
                result.add(property.getContents());
            }
            return type.convert(result);
        }
        return Collections.emptyList();
    }

    @Override
    public <T> boolean receiveListFromServer(String command, List<T> list, Type<T> type) {
        boolean rc = super.receiveListFromServer(command, list, type);
        if (rc) {
            return true;
        }
        if (CLIENTCMD_GETFLUIDS.equals(command)) {
            for (int i = 0 ; i < TANKS ; i++) {
                properties[i].set((FluidStack)list.get(i));
            }
            return true;
        }
        return false;
    }


    private MultiTankHandler handler = null;

    private MultiTankHandler createFluidHandler() {
        return new MultiTankHandler(this);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, Direction facing) {
        if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return fluidHandler.cast();
        }
        return super.getCapability(cap, facing);
    }

}
