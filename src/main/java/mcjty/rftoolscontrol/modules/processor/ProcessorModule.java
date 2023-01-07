package mcjty.rftoolscontrol.modules.processor;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.datagen.DataGen;
import mcjty.lib.datagen.Dob;
import mcjty.lib.modules.IModule;
import mcjty.rftoolscontrol.modules.processor.blocks.ProcessorBlock;
import mcjty.rftoolscontrol.modules.processor.blocks.ProcessorContainer;
import mcjty.rftoolscontrol.modules.processor.blocks.ProcessorTileEntity;
import mcjty.rftoolscontrol.modules.processor.client.GuiProcessor;
import mcjty.rftoolscontrol.modules.processor.client.ProcessorRenderer;
import mcjty.rftoolscontrol.modules.processor.items.*;
import mcjty.rftoolscontrol.modules.various.VariousModule;
import mcjty.rftoolscontrol.setup.Registration;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.RegistryObject;

import static mcjty.lib.datagen.DataGen.has;
import static mcjty.rftoolscontrol.setup.Registration.*;

public class ProcessorModule implements IModule {

    public static final RegistryObject<BaseBlock> PROCESSOR = BLOCKS.register("processor", ProcessorBlock::new);
    public static final RegistryObject<Item> PROCESSOR_ITEM = ITEMS.register("processor", () -> new BlockItem(PROCESSOR.get(), Registration.createStandardProperties()));
    public static final RegistryObject<BlockEntityType<ProcessorTileEntity>> TYPE_PROCESSOR = TILES.register("processor", () -> BlockEntityType.Builder.of(ProcessorTileEntity::new, PROCESSOR.get()).build(null));
    public static final RegistryObject<MenuType<ProcessorContainer>> PROCESSOR_CONTAINER = CONTAINERS.register("processor", GenericContainer::createContainerType);
    public static final RegistryObject<MenuType<ProcessorContainer>> PROCESSOR_CONTAINER_REMOTE = CONTAINERS.register("processor_remote",
            () -> GenericContainer.createRemoteContainerType(ProcessorTileEntity::new, ProcessorContainer::createRemote, ProcessorContainer.SLOTS));

    public static final RegistryObject<CPUCoreItem> CPU_CORE_500 = ITEMS.register("cpu_core_500", () -> new CPUCoreItem(0));
    public static final RegistryObject<CPUCoreItem> CPU_CORE_1000 = ITEMS.register("cpu_core_1000", () -> new CPUCoreItem(1));
    public static final RegistryObject<CPUCoreItem> CPU_CORE_2000 = ITEMS.register("cpu_core_2000", () -> new CPUCoreItem(2));
    public static final RegistryObject<RAMChipItem> RAM_CHIP = ITEMS.register("ram_chip", RAMChipItem::new);
    public static final RegistryObject<NetworkCardItem> NETWORK_CARD = ITEMS.register("network_card", () -> new NetworkCardItem(NetworkCardItem.TIER_NORMAL));
    public static final RegistryObject<NetworkCardItem> ADVANCED_NETWORK_CARD = ITEMS.register("advanced_network_card", () -> new NetworkCardItem(NetworkCardItem.TIER_ADVANCED));
    public static final RegistryObject<NetworkIdentifierItem> NETWORK_IDENTIFIER = ITEMS.register("network_identifier", NetworkIdentifierItem::new);
    public static final RegistryObject<GraphicsCardItem> GRAPHICS_CARD = ITEMS.register("graphics_card", GraphicsCardItem::new);

    @Override
    public void init(FMLCommonSetupEvent event) {

    }

    @Override
    public void initClient(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            GuiProcessor.register();
        });

        ProcessorRenderer.register();
    }

    @Override
    public void initConfig() {

    }

    @Override
    public void initDatagen(DataGen dataGen) {
        dataGen.add(
                Dob.blockBuilder(PROCESSOR)
                        .ironPickaxeTags()
                        .parentedItem("block/processor")
                        .standardLoot(TYPE_PROCESSOR)
                        .blockState(p -> p.orientedBlock(PROCESSOR.get(), p.frontBasedModel("processor", p.modLoc("block/machineprocessoron"))))
                        .shaped(builder -> builder
                                        .define('F', mcjty.rftoolsbase.modules.various.VariousModule.MACHINE_FRAME.get())
                                        .define('M', VariousModule.CARD_BASE.get())
                                        .define('q', Tags.Items.GEMS_QUARTZ)
                                        .unlockedBy("cardbase", has(VariousModule.CARD_BASE.get())),
                                "rqr", "MFM", "rqr"),
                Dob.itemBuilder(CPU_CORE_500)
                        .generatedItem("item/cpucoreb500")
                        .shaped(builder -> builder
                                        .define('M', VariousModule.CARD_BASE.get())
                                        .define('g', Tags.Items.NUGGETS_GOLD)
                                        .unlockedBy("cardbase", has(VariousModule.CARD_BASE.get())),
                                "rgr", "pMp", "rgr"),
                Dob.itemBuilder(CPU_CORE_1000)
                        .generatedItem("item/cpucores1000")
                        .shaped(builder -> builder
                                        .define('M', CPU_CORE_500.get())
                                        .unlockedBy("core500", has(CPU_CORE_500.get())),
                                "rdr", "eMe", "rdr"),
                Dob.itemBuilder(CPU_CORE_2000)
                        .generatedItem("item/cpucoreex2000")
                        .shaped(builder -> builder
                                .define('s', mcjty.rftoolsbase.modules.various.VariousModule.DIMENSIONALSHARD.get())

                                        .define('M', CPU_CORE_1000.get())
                                        .unlockedBy("core1000", has(CPU_CORE_1000.get())),
                                "rsr", "sMs", "rsr"),
                Dob.itemBuilder(RAM_CHIP)
                        .generatedItem("item/ramchip")
                        .shaped(builder -> builder
                                        .define('M', VariousModule.CARD_BASE.get())
                                        .unlockedBy("cardbase", has(VariousModule.CARD_BASE.get())),
                                "rrr", "pMp", "rrr"),
                Dob.itemBuilder(NETWORK_CARD)
                        .generatedItem("item/networkcard")
                        .shaped(builder -> builder
                                        .define('M', VariousModule.CARD_BASE.get())
                                        .define('g', Tags.Items.NUGGETS_GOLD)
                                        .unlockedBy("cardbase", has(VariousModule.CARD_BASE.get())),
                                "ror", "gMg", "rrr"),
                Dob.itemBuilder(ADVANCED_NETWORK_CARD)
                        .generatedItem("item/advancednetworkcard")
                        .shaped(builder -> builder
                                        .define('M', NETWORK_CARD.get())
                                        .unlockedBy("network_card", has(NETWORK_CARD.get())),
                                "ror", "eMe", "ror"),
                Dob.itemBuilder(NETWORK_IDENTIFIER)
                        .generatedItem("item/networkidentifier")
                        .shaped(builder -> builder
                                        .define('M', VariousModule.CARD_BASE.get())
                                        .define('X', Items.REPEATER)
                                        .define('C', Items.COMPARATOR)
                                        .unlockedBy("cardbase", has(VariousModule.CARD_BASE.get())),
                                " C ", " M ", " X "),
                Dob.itemBuilder(GRAPHICS_CARD)
                        .generatedItem("item/graphicscard")
                        .shaped(builder -> builder
                                        .define('M', VariousModule.CARD_BASE.get())
                                        .define('q', Tags.Items.GEMS_QUARTZ)
                                        .define('g', Tags.Items.DUSTS_GLOWSTONE)
                                        .unlockedBy("cardbase", has(VariousModule.CARD_BASE.get())),
                                "qqq", "rMr", "qgq")
        );
    }
}
