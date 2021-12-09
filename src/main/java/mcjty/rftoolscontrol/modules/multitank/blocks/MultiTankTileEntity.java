package mcjty.rftoolscontrol.modules.multitank.blocks;

import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.blockcommands.ListCommand;
import mcjty.lib.blockcommands.ServerCommand;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.tileentity.Cap;
import mcjty.lib.tileentity.CapType;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.rftoolscontrol.modules.multitank.MultiTankModule;
import mcjty.rftoolscontrol.modules.multitank.util.MultiTankFluidProperties;
import mcjty.rftoolscontrol.modules.multitank.util.MultiTankHandler;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static mcjty.lib.api.container.DefaultContainerProvider.empty;
import static mcjty.rftoolscontrol.modules.multitank.MultiTankModule.MULTITANK_CONTAINER;

public class MultiTankTileEntity extends GenericTileEntity {

    public static final int TANKS = 4;
    public static final int MAXCAPACITY = 10000;

    private final MultiTankFluidProperties properties[] = new MultiTankFluidProperties[TANKS];

    @Cap(type = CapType.FLUIDS)
    private final LazyOptional<MultiTankHandler> fluidHandler = LazyOptional.of(this::createFluidHandler);

    @Cap(type = CapType.CONTAINER)
    private final LazyOptional<INamedContainerProvider> screenHandler = LazyOptional.of(() -> new DefaultContainerProvider<GenericContainer>("Multi tank")
            .containerSupplier(empty(MULTITANK_CONTAINER, this))
            .setupSync(this));

    public MultiTankTileEntity() {
        super(MultiTankModule.MULTITANK_TILE.get());
        for (int i = 0 ; i < TANKS ; i++) {
            properties[i] = new MultiTankFluidProperties(this, FluidStack.EMPTY, MAXCAPACITY);
        }
    }

    public MultiTankFluidProperties[] getProperties() {
        return properties;
    }

    @Override
    protected void loadInfo(CompoundNBT tagCompound) {
        super.loadInfo(tagCompound);
        CompoundNBT info = tagCompound.getCompound("Info");
        for (int i = 0 ; i < TANKS ; i++) {
            properties[i] = new MultiTankFluidProperties(this, FluidStack.loadFluidStackFromNBT(info.getCompound("f" + i)), MAXCAPACITY);
        }
    }

    @Override
    protected void saveInfo(CompoundNBT tagCompound) {
        super.saveInfo(tagCompound);
        CompoundNBT info = getOrCreateInfo(tagCompound);
        for (int i = 0 ; i < TANKS ; i++) {
            FluidStack contents = properties[i].getContents();
            if (!contents.isEmpty()) {
                CompoundNBT tag = new CompoundNBT();
                contents.writeToNBT(tag);
                info.put("f" + i, tag);
            }
        }
    }

    @ServerCommand(type = FluidStack.class)
    public static final ListCommand<?, ?> CMD_GETFLUIDS = ListCommand.<MultiTankTileEntity, FluidStack>create("rftoolscontrol.tank.getFluids",
            (te, player, params) -> {
                List<FluidStack> result = new ArrayList<>(TANKS);
                for (MultiTankFluidProperties property : te.properties) {
                    result.add(property.getContents());
                }
                return result;
            },
            (te, player, params, list) -> {
                for (int i = 0 ; i < TANKS ; i++) {
                    te.properties[i].set(list.get(i));
                }
            });

    private MultiTankHandler handler = null;

    @Nonnull
    private MultiTankHandler createFluidHandler() {
        return new MultiTankHandler(this);
    }
}
