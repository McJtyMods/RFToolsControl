package mcjty.rftoolscontrol.modules.multitank.blocks;

import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.blockcommands.ListCommand;
import mcjty.lib.blockcommands.ServerCommand;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.tileentity.Cap;
import mcjty.lib.tileentity.CapType;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.rftoolscontrol.modules.multitank.MultiTankModule;
import mcjty.rftoolscontrol.modules.multitank.data.MultiTankData;
import mcjty.rftoolscontrol.modules.multitank.util.MultiTankFluidProperties;
import mcjty.rftoolscontrol.modules.multitank.util.MultiTankHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static mcjty.lib.api.container.DefaultContainerProvider.empty;
import static mcjty.rftoolscontrol.modules.multitank.MultiTankModule.MULTITANK_CONTAINER;

public class MultiTankTileEntity extends GenericTileEntity {

    public static final int TANKS = 4;
    public static final int MAXCAPACITY = 10000;

    private final MultiTankFluidProperties[] properties = new MultiTankFluidProperties[TANKS];

    private final MultiTankHandler fluidHandler = createFluidHandler();
    @Cap(type = CapType.FLUIDS)
    private final static Function<MultiTankTileEntity, MultiTankHandler> FLUID_CAP = tile -> tile.fluidHandler;

    @Cap(type = CapType.CONTAINER)
    private static final Function<MultiTankTileEntity, MenuProvider> SCREEN_CAP = tile -> new DefaultContainerProvider<GenericContainer>("Multi tank")
            .containerSupplier(empty(MULTITANK_CONTAINER, tile))
            .setupSync(tile);

    public MultiTankTileEntity(BlockPos pos, BlockState state) {
        super(MultiTankModule.MULTITANK.be().get(), pos, state);
        for (int i = 0 ; i < TANKS ; i++) {
            properties[i] = new MultiTankFluidProperties(this, FluidStack.EMPTY, MAXCAPACITY);
        }
        updateTankDataFromProperties();
    }

    public MultiTankFluidProperties[] getProperties() {
        return properties;
    }

    @Override
    public void loadAdditional(CompoundTag tagCompound, HolderLookup.Provider provider) {
        super.loadAdditional(tagCompound, provider);
        readFluids(tagCompound, provider);
    }

    @Override
    public void saveAdditional(@Nonnull CompoundTag tagCompound, HolderLookup.Provider provider) {
        super.saveAdditional(tagCompound, provider);
        writeFluids(tagCompound, provider);
    }

    private void readFluids(CompoundTag tagCompound, HolderLookup.Provider provider) {
        ListTag list = tagCompound.getList("fluids", Tag.TAG_COMPOUND);
        List<FluidStack> fluids = new ArrayList<>(TANKS);
        for (int i = 0; i < TANKS; i++) {
            CompoundTag fluidTag;
            if (i < list.size()) {
                fluidTag = list.getCompound(i);
            } else if (tagCompound.contains("tank" + i, Tag.TAG_COMPOUND)) { // Legacy support
                fluidTag = tagCompound.getCompound("tank" + i);
            } else {
                fluidTag = new CompoundTag();
            }
            FluidStack stack = FluidStack.parseOptional(provider, fluidTag);
            properties[i].loadFromData(stack);
            fluids.add(stack);
        }
        setData(MultiTankModule.MULTITANK_DATA, MultiTankData.of(fluids));
    }

    private void writeFluids(CompoundTag tagCompound, HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        for (int i = 0; i < TANKS; i++) {
            CompoundTag fluidTag = new CompoundTag();
            FluidStack stack = properties[i].getContentsInternal();
            if (!stack.isEmpty()) {
                stack.save(provider, fluidTag);
            }
            list.add(fluidTag);
        }
        tagCompound.put("fluids", list);
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

    @Nonnull
    private MultiTankHandler createFluidHandler() {
        return new MultiTankHandler(this);
    }

    public void onTankContentsChanged() {
        updateTankDataFromProperties();
        setChanged();
    }

    private void updateTankDataFromProperties() {
        if (level != null && level.isClientSide) {
            return;
        }
        List<FluidStack> fluids = new ArrayList<>(TANKS);
        for (MultiTankFluidProperties property : properties) {
            fluids.add(property.getContents());
        }
        setData(MultiTankModule.MULTITANK_DATA, MultiTankData.of(fluids));
    }

    private void applyTankData(MultiTankData data) {
        List<FluidStack> fluids = data.fluids();
        for (int i = 0; i < TANKS; i++) {
            FluidStack stack = i < fluids.size() ? fluids.get(i) : FluidStack.EMPTY;
            properties[i].loadFromData(stack);
        }
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput input) {
        super.applyImplicitComponents(input);
        MultiTankData data = input.get(MultiTankModule.ITEM_MULTITANK_DATA);
        if (data != null) {
            setData(MultiTankModule.MULTITANK_DATA, data);
            applyTankData(data);
        }
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        builder.set(MultiTankModule.ITEM_MULTITANK_DATA, getData(MultiTankModule.MULTITANK_DATA));
    }
}
