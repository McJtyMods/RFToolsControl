package mcjty.rftoolscontrol.modules.various;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.container.GenericContainer;
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
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import static mcjty.rftoolscontrol.setup.Registration.*;

public class VariousModule implements IModule {

    public static final RegistryObject<BaseBlock> NODE = BLOCKS.register("node", NodeBlock::new);
    public static final RegistryObject<BlockEntityType<NodeTileEntity>> NODE_TILE = TILES.register("node", () -> BlockEntityType.Builder.of(NodeTileEntity::new, NODE.get()).build(null));
    public static final RegistryObject<Item> NODE_ITEM = ITEMS.register("node", () -> new BlockItem(NODE.get(), Registration.createStandardProperties()));
    public static final RegistryObject<MenuType<GenericContainer>> NODE_CONTAINER = CONTAINERS.register("node", GenericContainer::createContainerType);

    public static final RegistryObject<BaseBlock> WORKBENCH = BLOCKS.register("workbench", WorkbenchBlock::new);
    public static final RegistryObject<BlockEntityType<WorkbenchTileEntity>> WORKBENCH_TILE = TILES.register("workbench", () -> BlockEntityType.Builder.of(WorkbenchTileEntity::new, WORKBENCH.get()).build(null));
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
}
