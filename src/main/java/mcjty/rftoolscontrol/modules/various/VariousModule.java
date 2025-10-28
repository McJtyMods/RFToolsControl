package mcjty.rftoolscontrol.modules.various;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.datagen.DataGen;
import mcjty.lib.datagen.Dob;
import mcjty.lib.modules.IModule;
import mcjty.rftoolsbase.modules.tablet.items.TabletItem;
import mcjty.rftoolscontrol.modules.processor.logic.grid.ProgramCardInstance;
import mcjty.rftoolscontrol.modules.programmer.client.GuiProgrammer;
import mcjty.rftoolscontrol.modules.various.blocks.*;
import mcjty.rftoolscontrol.modules.various.client.GuiNode;
import mcjty.rftoolscontrol.modules.various.client.GuiWorkbench;
import mcjty.rftoolscontrol.modules.various.data.NodeData;
import mcjty.rftoolscontrol.modules.various.data.WorkbenchData;
import mcjty.rftoolscontrol.modules.various.items.CardBaseItem;
import mcjty.rftoolscontrol.modules.various.items.ProgramCardItem;
import mcjty.rftoolscontrol.modules.various.items.TokenItem;
import mcjty.rftoolscontrol.modules.various.items.consolemodule.ConsoleModuleItem;
import mcjty.rftoolscontrol.modules.various.items.consolemodule.ConsoleScreenModule;
import mcjty.rftoolscontrol.modules.various.items.interactionmodule.InteractionModuleItem;
import mcjty.rftoolscontrol.modules.various.items.interactionmodule.InteractionScreenModule;
import mcjty.rftoolscontrol.modules.various.items.variablemodule.VariableModuleItem;
import mcjty.rftoolscontrol.modules.various.items.variablemodule.VariableScreenModule;
import mcjty.rftoolscontrol.modules.various.items.vectorartmodule.VectorArtModuleItem;
import mcjty.rftoolscontrol.modules.various.items.vectorartmodule.VectorArtScreenModule;
import mcjty.rftoolscontrol.setup.Registration;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.function.Supplier;

import static mcjty.lib.datagen.DataGen.has;
import static mcjty.rftoolscontrol.RFToolsControl.tab;
import static mcjty.rftoolscontrol.setup.Registration.*;

public class VariousModule implements IModule {

    public static final RBlock<BaseBlock, BlockItem, NodeTileEntity> NODE = RBLOCKS.registerBlock(
            "node",
            NodeTileEntity.class,
            NodeBlock::new,
            block -> new BlockItem(block.get(), Registration.createStandardProperties()),
            NodeTileEntity::new
    );
    public static final Supplier<MenuType<GenericContainer>> NODE_CONTAINER = CONTAINERS.register("node", GenericContainer::createContainerType);

    public static final RBlock<BaseBlock, BlockItem, WorkbenchTileEntity> WORKBENCH = RBLOCKS.registerBlock(
            "workbench",
            WorkbenchTileEntity.class,
            WorkbenchBlock::new,
            block -> new BlockItem(block.get(), Registration.createStandardProperties()),
            WorkbenchTileEntity::new
    );
    public static final Supplier<MenuType<WorkbenchContainer>> WORKBENCH_CONTAINER = CONTAINERS.register("workbench", GenericContainer::createContainerType);

    public static final DeferredItem<CardBaseItem> CARD_BASE = ITEMS.register("card_base", tab(CardBaseItem::new));
    public static final DeferredItem<TokenItem> TOKEN = ITEMS.register("token", tab(TokenItem::new));

    // Data component for token parameter storage
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<mcjty.rftoolscontrol.modules.various.data.TokenData>> TOKEN_DATA = COMPONENTS.registerComponentType(
            "token_data",
            builder -> builder.persistent(mcjty.rftoolscontrol.modules.various.data.TokenData.CODEC).networkSynchronized(mcjty.rftoolscontrol.modules.various.data.TokenData.STREAM_CODEC)
    );

    public static final Supplier<AttachmentType<NodeData>> NODE_DATA = ATTACHMENT_TYPES.register(
            "node_data", () -> AttachmentType.builder(NodeData::createDefault)
                    .serialize(NodeData.CODEC)
                    .build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<NodeData>> ITEM_NODE_DATA = COMPONENTS.registerComponentType(
            "node_data",
            builder -> builder.persistent(NodeData.CODEC)
                    .networkSynchronized(NodeData.STREAM_CODEC)
    );

    public static final Supplier<AttachmentType<WorkbenchData>> WORKBENCH_DATA = ATTACHMENT_TYPES.register(
            "workbench_data", () -> AttachmentType.builder(WorkbenchData::createDefault)
                    .serialize(WorkbenchData.CODEC)
                    .build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<WorkbenchData>> ITEM_WORKBENCH_DATA = COMPONENTS.registerComponentType(
            "workbench_data",
            builder -> builder.persistent(WorkbenchData.CODEC)
                    .networkSynchronized(WorkbenchData.STREAM_CODEC)
    );

    public static final DeferredItem<ProgramCardItem> PROGRAM_CARD = ITEMS.register("program_card", tab(ProgramCardItem::new));
    public static final DeferredItem<VariableModuleItem> VARIABLE_MODULE = ITEMS.register("variable_module", tab(VariableModuleItem::new));
    public static final DeferredItem<InteractionModuleItem> INTERACTION_MODULE = ITEMS.register("interaction_module", tab(InteractionModuleItem::new));
    public static final DeferredItem<ConsoleModuleItem> CONSOLE_MODULE = ITEMS.register("console_module", tab(ConsoleModuleItem::new));
    public static final DeferredItem<VectorArtModuleItem> VECTORART_MODULE = ITEMS.register("vectorart_module", tab(VectorArtModuleItem::new));
    public static final DeferredItem<TabletItem> TABLET_PROCESSOR = ITEMS.register("tablet_processor", tab(TabletItem::new));

    // Data component for interaction module configuration
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<InteractionScreenModule>> INTERACTION_MODULE_DATA = COMPONENTS.registerComponentType(
            "interaction_module_data",
            builder -> builder.persistent(InteractionScreenModule.CODEC)
                    .networkSynchronized(InteractionScreenModule.STREAM_CODEC)
    );

    // Data component for variable module configuration
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<VariableScreenModule>> VARIABLE_MODULE_DATA = COMPONENTS.registerComponentType(
            "variable_module_data",
            builder -> builder.persistent(VariableScreenModule.CODEC)
                    .networkSynchronized(VariableScreenModule.STREAM_CODEC)
    );

    // Data component for vector art module configuration
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<VectorArtScreenModule>> VECTORART_MODULE_DATA = COMPONENTS.registerComponentType(
            "vectorart_module_data",
            builder -> builder.persistent(VectorArtScreenModule.CODEC)
                    .networkSynchronized(VectorArtScreenModule.STREAM_CODEC)
    );

    // Data component for console module configuration
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ConsoleScreenModule>> CONSOLE_MODULE_DATA = COMPONENTS.registerComponentType(
            "console_module_data",
            builder -> builder.persistent(ConsoleScreenModule.CODEC)
                    .networkSynchronized(ConsoleScreenModule.STREAM_CODEC)
    );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ProgramCardInstance>> PROGRAM_CARD_DATA = COMPONENTS.registerComponentType(
            "program_card_data",
            builder -> builder.persistent(ProgramCardInstance.CODEC).networkSynchronized(ProgramCardInstance.STREAM_CODEC)
    );

    // Name of the program card
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> PROGRAM_CARD_NAME = COMPONENTS.registerComponentType(
            "program_card_name",
            builder -> builder.persistent(com.mojang.serialization.Codec.STRING).networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.STRING_UTF8)
    );

    public VariousModule(IEventBus bus) {
        bus.addListener(this::registerMenuScreens);
    }

    @Override
    public void init(FMLCommonSetupEvent event) {

    }

    @Override
    public void initClient(FMLClientSetupEvent event) {
    }

    public void registerMenuScreens(RegisterMenuScreensEvent event) {
        GuiWorkbench.register(event);
        GuiNode.register(event);
    }

    @Override
    public void initConfig(IEventBus bus) {

    }

    @Override
    public void initDatagen(DataGen dataGen, HolderLookup.Provider registries) {
        dataGen.add(
                Dob.blockBuilder(WORKBENCH)
                        .ironPickaxeTags()
                        .parentedItem("block/workbench")
                        .standardLoot(VariousModule.ITEM_WORKBENCH_DATA.get())
                        .blockState(p -> p.orientedBlock(WORKBENCH.block().get(), p.frontBasedModel("workbench", p.modLoc("block/machineworkbench"))))
                        .shaped(builder -> builder
                                        .define('F', mcjty.rftoolsbase.modules.various.VariousModule.MACHINE_FRAME.get())
                                        .define('C', Items.CRAFTING_TABLE)
                                        .define('X', Items.CHEST)
                                        .unlockedBy("frame", has(mcjty.rftoolsbase.modules.various.VariousModule.MACHINE_FRAME.get())),
                                " C ", " F ", " X "),
                Dob.blockBuilder(NODE)
                        .ironPickaxeTags()
                        .parentedItem("block/node")
                        .standardLoot(VariousModule.ITEM_NODE_DATA.get())
                        .blockState(p -> p.orientedBlock(NODE.block().get(), p.frontBasedModel("node", p.modLoc("block/machinenode"))))
                        .shaped(builder -> builder
                                        .define('F', mcjty.rftoolsbase.modules.various.VariousModule.MACHINE_FRAME.get())
                                        .define('M', VariousModule.CARD_BASE.get())
                                        .unlockedBy("cardbase", has(VariousModule.CARD_BASE.get())),
                                "ror", "rFr", "rMr"),
                Dob.itemBuilder(PROGRAM_CARD)
                        .generatedItem("item/programcard")
                        .shaped(builder -> builder
                                        .define('M', VariousModule.CARD_BASE.get())
                                        .unlockedBy("cardbase", has(VariousModule.CARD_BASE.get())),
                                "pp", "Mp"),
                Dob.itemBuilder(CARD_BASE)
                        .generatedItem("item/cardbase")
                        .shaped(builder -> builder
                                        .define('n', Tags.Items.DYES_GREEN)
                                        .define('g', Tags.Items.NUGGETS_GOLD)
                                        .unlockedBy("redstone", has(Items.REDSTONE)),
                                "rrr", "nnn", "ggg"),
                Dob.itemBuilder(TOKEN)
                        .generatedItem("item/token")
                        .shaped(builder -> builder
                                        .define('M', VariousModule.CARD_BASE.get())
                                        .unlockedBy("cardbase", has(VariousModule.CARD_BASE.get())),
                                "ppp", "pMp", "ppp"),
                Dob.itemBuilder(VARIABLE_MODULE)
                        .generatedItem("item/variablemoduleitem")
                        .shaped(builder -> builder
                                        .define('M', VariousModule.CARD_BASE.get())
                                        .define('z', Tags.Items.DYES_BLACK)
                                        .unlockedBy("cardbase", has(VariousModule.CARD_BASE.get())),
                                " M ", "rir", " z "),
                Dob.itemBuilder(INTERACTION_MODULE)
                        .generatedItem("item/interactionmoduleitem")
                        .shaped(builder -> builder
                                        .define('M', VariousModule.CARD_BASE.get())
                                        .define('z', Tags.Items.DYES_BLACK)
                                        .define('P', Items.STONE_PRESSURE_PLATE)
                                        .unlockedBy("cardbase", has(VariousModule.CARD_BASE.get())),
                                "PMP", "rir", " z "),
                Dob.itemBuilder(CONSOLE_MODULE)
                        .generatedItem("item/consolemoduleitem")
                        .shaped(builder -> builder
                                        .define('M', VariousModule.CARD_BASE.get())
                                        .define('P', Tags.Items.GLASS_PANES)
                                        .define('z', Tags.Items.DYES_BLACK)
                                        .unlockedBy("cardbase", has(VariousModule.CARD_BASE.get())),
                                "PMP", "rir", "PzP"),
                Dob.itemBuilder(VECTORART_MODULE)
                        .generatedItem("item/vectorartmoduleitem")
                        .shaped(builder -> builder
                                        .define('M', VariousModule.CARD_BASE.get())
                                        .define('z', Tags.Items.DYES_BLACK)
                                        .define('P', Tags.Items.GLASS_PANES)
                                        .define('I', Tags.Items.INGOTS_GOLD)
                                        .unlockedBy("cardbase", has(VariousModule.CARD_BASE.get())),
                                "PMP", "rIr", "PzP"),
                Dob.itemBuilder(TABLET_PROCESSOR)
                        // @todo recipe missing?
                        .generatedItem("item/tablet_processor")
        );
    }
}
