package mcjty.rftoolscontrol.modules.various;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.datagen.DataGen;
import mcjty.lib.datagen.Dob;
import mcjty.lib.modules.IModule;
import mcjty.rftoolsbase.modules.tablet.items.TabletItem;
import mcjty.rftoolscontrol.modules.various.blocks.*;
import mcjty.rftoolscontrol.modules.various.client.GuiNode;
import mcjty.rftoolscontrol.modules.various.client.GuiWorkbench;
import mcjty.rftoolscontrol.modules.various.items.CardBaseItem;
import mcjty.rftoolscontrol.modules.various.items.ProgramCardItem;
import mcjty.rftoolscontrol.modules.various.items.TokenItem;
import mcjty.rftoolscontrol.modules.various.items.consolemodule.ConsoleModuleItem;
import mcjty.rftoolscontrol.modules.various.items.interactionmodule.InteractionModuleItem;
import mcjty.rftoolscontrol.modules.various.items.variablemodule.VariableModuleItem;
import mcjty.rftoolscontrol.modules.various.items.vectorartmodule.VectorArtModuleItem;
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

public class VariousModule implements IModule {

    public static final RegistryObject<BaseBlock> NODE = BLOCKS.register("node", NodeBlock::new);
    public static final RegistryObject<BlockEntityType<NodeTileEntity>> TYPE_NODE = TILES.register("node", () -> BlockEntityType.Builder.of(NodeTileEntity::new, NODE.get()).build(null));
    public static final RegistryObject<Item> NODE_ITEM = ITEMS.register("node", () -> new BlockItem(NODE.get(), Registration.createStandardProperties()));
    public static final RegistryObject<MenuType<GenericContainer>> NODE_CONTAINER = CONTAINERS.register("node", GenericContainer::createContainerType);

    public static final RegistryObject<BaseBlock> WORKBENCH = BLOCKS.register("workbench", WorkbenchBlock::new);
    public static final RegistryObject<BlockEntityType<WorkbenchTileEntity>> TYPE_WORKBENCH = TILES.register("workbench", () -> BlockEntityType.Builder.of(WorkbenchTileEntity::new, WORKBENCH.get()).build(null));
    public static final RegistryObject<Item> WORKBENCH_ITEM = ITEMS.register("workbench", () -> new BlockItem(WORKBENCH.get(), Registration.createStandardProperties()));
    public static final RegistryObject<MenuType<WorkbenchContainer>> WORKBENCH_CONTAINER = CONTAINERS.register("workbench", GenericContainer::createContainerType);

    public static final RegistryObject<CardBaseItem> CARD_BASE = ITEMS.register("card_base", CardBaseItem::new);
    public static final RegistryObject<TokenItem> TOKEN = ITEMS.register("token", TokenItem::new);

    public static final RegistryObject<ProgramCardItem> PROGRAM_CARD = ITEMS.register("program_card", ProgramCardItem::new);
    public static final RegistryObject<VariableModuleItem> VARIABLE_MODULE = ITEMS.register("variable_module", VariableModuleItem::new);
    public static final RegistryObject<InteractionModuleItem> INTERACTION_MODULE = ITEMS.register("interaction_module", InteractionModuleItem::new);
    public static final RegistryObject<ConsoleModuleItem> CONSOLE_MODULE = ITEMS.register("console_module", ConsoleModuleItem::new);
    public static final RegistryObject<VectorArtModuleItem> VECTORART_MODULE = ITEMS.register("vectorart_module", VectorArtModuleItem::new);

    public static final RegistryObject<TabletItem> TABLET_PROCESSOR = ITEMS.register("tablet_processor", TabletItem::new);

    @Override
    public void init(FMLCommonSetupEvent event) {

    }

    @Override
    public void initClient(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            GuiWorkbench.register();
            GuiNode.register();
        });
    }

    @Override
    public void initConfig() {

    }

    @Override
    public void initDatagen(DataGen dataGen) {
        dataGen.add(
                Dob.blockBuilder(WORKBENCH)
                        .ironPickaxeTags()
                        .parentedItem("block/workbench")
                        .standardLoot(TYPE_WORKBENCH)
                        .blockState(p -> p.orientedBlock(WORKBENCH.get(), p.frontBasedModel("workbench", p.modLoc("block/machineworkbench"))))
                        .shaped(builder -> builder
                                        .define('F', mcjty.rftoolsbase.modules.various.VariousModule.MACHINE_FRAME.get())
                                        .define('C', Items.CRAFTING_TABLE)
                                        .define('X', Items.CHEST)
                                        .unlockedBy("frame", has(mcjty.rftoolsbase.modules.various.VariousModule.MACHINE_FRAME.get())),
                                " C ", " F ", " X "),
                Dob.blockBuilder(NODE)
                        .ironPickaxeTags()
                        .parentedItem("block/node")
                        .standardLoot(TYPE_NODE)
                        .blockState(p -> p.orientedBlock(NODE.get(), p.frontBasedModel("node", p.modLoc("block/machinenode"))))
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
