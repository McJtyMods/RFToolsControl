package mcjty.rftoolscontrol.modules.programmer.blocks;


import com.mojang.serialization.MapCodec;
import java.util.function.Function;
import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.container.GenericItemHandler;
import mcjty.lib.tileentity.Cap;
import mcjty.lib.tileentity.CapType;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.varia.Lazy;
import mcjty.rftoolscontrol.modules.processor.logic.grid.ProgramCardInstance;
import mcjty.rftoolscontrol.modules.programmer.ProgrammerModule;
import mcjty.rftoolscontrol.modules.various.VariousModule;
import mcjty.rftoolscontrol.modules.various.items.ProgramCardItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import static mcjty.lib.api.container.DefaultContainerProvider.container;
import static mcjty.lib.container.SlotDefinition.generic;
import static mcjty.lib.container.SlotDefinition.specific;
import static mcjty.lib.setup.Registration.ITEM_INVENTORY;
import static mcjty.rftoolscontrol.modules.programmer.ProgrammerModule.PROGRAMMER_CONTAINER;


public class ProgrammerTileEntity extends GenericTileEntity {

    public static final int SLOT_CARD = 0;
    public static final int SLOT_DUMMY = 1;
    public static final Lazy<ContainerFactory> CONTAINER_FACTORY = Lazy.of(() -> new ContainerFactory(2)
            .box(specific(new ItemStack(VariousModule.PROGRAM_CARD.get())).in().out(), SLOT_CARD, 91, 136, 1, 1)
            .box(generic(), SLOT_DUMMY, -1000, -1000, 1, 1)
            .playerSlots(91, 157));

    private final GenericItemHandler items = GenericItemHandler.basic(this, CONTAINER_FACTORY);
    @Cap(type = CapType.ITEMS_AUTOMATION)
    private static final Function<ProgrammerTileEntity, GenericItemHandler> ITEM_CAP = tile -> tile.items;

    @Cap(type = CapType.CONTAINER)
    private static final Function<ProgrammerTileEntity, MenuProvider> SCREEN_CAP = tile -> new DefaultContainerProvider<GenericContainer>("Programmer")
            .containerSupplier(container(PROGRAMMER_CONTAINER, CONTAINER_FACTORY, tile))
            .itemHandler(() -> tile.items)
            .setupSync(tile);

    public ProgrammerTileEntity(BlockPos pos, BlockState state) {
        super(ProgrammerModule.PROGRAMMER.be().get(), pos, state);
        items.setStackInSlot(SLOT_DUMMY, new ItemStack(VariousModule.PROGRAM_CARD.get()));
    }

    public GenericItemHandler getItems() {
        return items;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        CompoundTag tag = new CompoundTag();
        HolderLookup.Provider provider = level == null ? RegistryAccess.EMPTY : level.registryAccess();
        items.save(tag, "items", provider);
        output.store(MapCodec.assumeMapUnsafe(CompoundTag.CODEC), tag);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        CompoundTag tag = input.read(MapCodec.assumeMapUnsafe(CompoundTag.CODEC)).orElseGet(CompoundTag::new);
        HolderLookup.Provider provider = input.lookup();
        items.load(tag, "items", provider);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter input) {
        super.applyImplicitComponents(input);
        items.applyImplicitComponents(input.get(ITEM_INVENTORY.get()));
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        items.collectImplicitComponents(builder);
    }


    @Override
    public void setPowerInput(int powered) {
        int p = powerLevel;
        super.setPowerInput(powered);
        if (p != powerLevel && powerLevel > 0) {
            // Copy program in programmer to card
            ItemStack dummy = items.getStackInSlot(SLOT_DUMMY);
            if (dummy.isEmpty()) {
                return;
            }
            ItemStack card = items.getStackInSlot(SLOT_CARD);
            if (card.isEmpty()) {
                return;
            }
            ProgramCardInstance instance = dummy.get(VariousModule.PROGRAM_CARD_DATA.get());
            if (instance == null) {
                return;
            }
            ProgramCardItem.setCardName(card, ProgramCardItem.getCardName(dummy));
            card.set(VariousModule.PROGRAM_CARD_DATA.get(), instance);
            items.setStackInSlot(SLOT_CARD, card);
            setChanged();
        }
    }

}
