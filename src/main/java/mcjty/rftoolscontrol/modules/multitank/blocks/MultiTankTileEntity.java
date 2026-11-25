package mcjty.rftoolscontrol.modules.multitank.blocks;

import com.mojang.serialization.MapCodec;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nonnull;
import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.blockcommands.ListCommand;
import mcjty.lib.blockcommands.ServerCommand;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.tileentity.Cap;
import mcjty.lib.tileentity.CapType;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.varia.FluidStack;
import mcjty.rftoolscontrol.modules.multitank.MultiTankModule;
import mcjty.rftoolscontrol.modules.multitank.data.MultiTankData;
import mcjty.rftoolscontrol.modules.multitank.util.MultiTankFluidProperties;
import mcjty.rftoolscontrol.modules.multitank.util.MultiTankHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

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
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        CompoundTag tagCompound = input.read(MapCodec.assumeMapUnsafe(CompoundTag.CODEC)).orElseGet(CompoundTag::new);
        HolderLookup.Provider provider = input.lookup();
        readFluids(tagCompound, provider);
    }

    @Override
    public void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        CompoundTag tagCompound = new CompoundTag();
        HolderLookup.Provider provider = level == null ? RegistryAccess.EMPTY : level.registryAccess();
        writeFluids(tagCompound, provider);
        output.store(MapCodec.assumeMapUnsafe(CompoundTag.CODEC), tagCompound);
    }

    private void readFluids(CompoundTag tagCompound, HolderLookup.Provider provider) {
        ListTag list = tagCompound.getListOrEmpty("fluids");
        List<FluidStack> fluids = new ArrayList<>(TANKS);
        for (int i = 0; i < TANKS; i++) {
            CompoundTag fluidTag;
            if (i < list.size()) {
                fluidTag = list.getCompoundOrEmpty(i);
            } else if (tagCompound.contains("tank" + i)) { // Legacy support
                fluidTag = tagCompound.getCompoundOrEmpty("tank" + i);
            } else {
                fluidTag = new CompoundTag();
            }
            FluidStack stack = FluidStack.OPTIONAL_CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), fluidTag).result().orElse(FluidStack.EMPTY);
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
                fluidTag = (CompoundTag) FluidStack.CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), stack).getOrThrow();
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
        if (level != null && level.isClientSide()) {
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
    protected void applyImplicitComponents(DataComponentGetter input) {
        super.applyImplicitComponents(input);
        MultiTankData data = input.get(MultiTankModule.ITEM_MULTITANK_DATA.get());
        if (data != null) {
            setData(MultiTankModule.MULTITANK_DATA, data);
            applyTankData(data);
        }
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        builder.set(MultiTankModule.ITEM_MULTITANK_DATA.get(), getData(MultiTankModule.MULTITANK_DATA));
    }
}
