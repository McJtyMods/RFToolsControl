package mcjty.rftoolscontrol.blocks.multitank;

import mcjty.lib.entity.GenericTileEntity;
import mcjty.lib.network.Argument;
import mcjty.rftoolscontrol.blocks.processor.GuiProcessor;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MultiTankTileEntity extends GenericTileEntity {

    public static final String CMD_GETFLUIDS = "getFluids";
    public static final String CLIENTCMD_GETFLUIDS = "getFluids";

    public static final int TANKS = 4;
    public static final int MAXCAPACITY = 10000;

    private final MultiTankFluidProperties properties[] = new MultiTankFluidProperties[TANKS];
    private final FluidStack fluids[] = new FluidStack[TANKS];

    public MultiTankTileEntity() {
        for (int i = 0 ; i < TANKS ; i++) {
            properties[i] = new MultiTankFluidProperties(this, null, MAXCAPACITY);
        }
    }

    public MultiTankFluidProperties[] getProperties() {
        return properties;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        return tagCompound;
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        for (int i = 0 ; i < TANKS ; i++) {
            properties[i] = new MultiTankFluidProperties(this, FluidStack.loadFluidStackFromNBT(tagCompound.getCompoundTag("f" + i)), MAXCAPACITY);
        }
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        for (int i = 0 ; i < TANKS ; i++) {
            FluidStack contents = properties[i].getContents();
            if (contents != null) {
                NBTTagCompound tag = new NBTTagCompound();
                contents.writeToNBT(tag);
                tagCompound.setTag("f" + i, tag);
            }
        }
    }

    @Override
    public List executeWithResultList(String command, Map<String, Argument> args) {
        List rc = super.executeWithResultList(command, args);
        if (rc != null) {
            return rc;
        }
        if (CMD_GETFLUIDS.equals(command)) {
            List<FluidStack> result = new ArrayList<>(TANKS);
            for (MultiTankFluidProperties property : properties) {
                result.add(property.getContents());
            }
            return result;
        }
        return null;
    }

    @Override
    public boolean execute(String command, List list) {
        boolean rc = super.execute(command, list);
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


    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return (T) new MultiTankHandler(this);
        }
        return super.getCapability(capability, facing);
    }

}
