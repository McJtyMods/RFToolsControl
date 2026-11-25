package mcjty.rftoolscontrol.setup;

import java.util.function.Supplier;
import mcjty.lib.blocks.RBlockRegistry;
import mcjty.lib.setup.DeferredItems;
import mcjty.lib.setup.DeferredRegister;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.modules.processor.ProcessorModule;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;

import static mcjty.rftoolscontrol.RFToolsControl.MODID;

public class Registration {

    public static final RBlockRegistry RBLOCKS = new RBlockRegistry(MODID, RFToolsControl.setup::addTabItem);
    public static final DeferredItems ITEMS = DeferredItems.create(MODID);
    public static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
    public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(Registries.MENU, MODID);
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, MODID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, MODID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MODID);

    public static void register() {
        RBLOCKS.register();
        ITEMS.register();
        TILES.register();
        CONTAINERS.register();
        SOUNDS.register();
        ENTITIES.register();
        TABS.register();
        COMPONENTS.register();
    }

    public static Item.Properties createStandardProperties() {
        return RFToolsControl.setup.defaultProperties();
    }

    public static Supplier<CreativeModeTab> TAB = TABS.register("rftoolscontrol", () -> FabricCreativeModeTab.builder()
            .title(Component.translatable("itemGroup." + MODID))
            .icon(() -> new ItemStack(ProcessorModule.PROCESSOR.block().get()))
            .displayItems((featureFlags, output) -> {
                RFToolsControl.setup.populateTab(output);
            })
            .build());
}
