package mcjty.rftoolscontrol.setup;
import mcjty.lib.blocks.RBlockRegistry;
import mcjty.lib.setup.DeferredItems;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.modules.processor.ProcessorModule;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

import static mcjty.rftoolscontrol.RFToolsControl.MODID;

public class Registration {

    public static final RBlockRegistry RBLOCKS = new RBlockRegistry(MODID, RFToolsControl.setup::addTabItem);
    public static final DeferredItems ITEMS = DeferredItems.create(MODID);
    public static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MODID);
    public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(BuiltInRegistries.MENU, MODID);
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, MODID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, MODID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static void register(IEventBus bus) {
        RBLOCKS.register(bus);
        ITEMS.register(bus);
        COMPONENTS.register(bus);
        CONTAINERS.register(bus);
        SOUNDS.register(bus);
        ENTITIES.register(bus);
        TABS.register(bus);
    }


    public static Item.Properties createStandardProperties() {
        return RFToolsControl.setup.defaultProperties();
    }

    public static Supplier<CreativeModeTab> TAB = TABS.register(MODID, () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup." + MODID))
            .icon(() -> new ItemStack(ProcessorModule.PROCESSOR.block().get()))
            .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
            .displayItems((featureFlags, output) -> {
                RFToolsControl.setup.populateTab(output);
            })
            .build());

//    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ProgramCardData>> PROGRAM_CARD_GRID_DATA = COMPONENTS.registerComponentType(
//            "program_card_grid",
//            builder -> builder
//                    .persistent(ProgramCardData.CODEC)
//                    .networkSynchronized(ProgramCardData.STREAM_CODEC)
//                    .build());
//
//    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ProgramCardNameData>> PROGRAM_CARD_NAME_DATA = COMPONENTS.registerComponentType(
//            "program_card_name",
//            builder -> builder
//                    .persistent(ProgramCardNameData.CODEC)
//                    .networkSynchronized(ProgramCardNameData.STREAM_CODEC)
//                    .build());
}
