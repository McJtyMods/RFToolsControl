package mcjty.rftoolscontrol.blocks.multitank;

import mcjty.lib.entity.GenericTileEntity;
import mcjty.lib.network.Argument;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import java.util.Map;

public class MultiTankTileEntity extends GenericTileEntity {

    public static final int TANKS = 4;
    public static final int MAXCAPACITY = 10000;

    private final IFluidTankProperties properties[] = new IFluidTankProperties[TANKS];

    public MultiTankTileEntity() {
        for (int i = 0 ; i < TANKS ; i++) {
            properties[i] = new FluidTankProperties(null, MAXCAPACITY);
        }
    }

    public IFluidTankProperties[] getProperties() {
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
            properties[i] = new FluidTankProperties(FluidStack.loadFluidStackFromNBT(tagCompound.getCompoundTag("f" + i)), MAXCAPACITY);
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
    public boolean execute(EntityPlayerMP playerMP, String command, Map<String, Argument> args) {
        boolean rc = super.execute(playerMP, command, args);
        if (rc) {
            return true;
        }
//        if (CMD_UPDATE.equals(command)) {
//            this.node = args.get("node").getString();
//            this.channel = args.get("channel").getString();
//            markDirtyClient();
//            return true;
//        }
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
