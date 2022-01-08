package mcjty.rftoolscontrol.modules.programmer;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.modules.IModule;
import mcjty.rftoolscontrol.modules.processor.logic.editors.ParameterEditors;
import mcjty.rftoolscontrol.modules.programmer.blocks.ProgrammerBlock;
import mcjty.rftoolscontrol.modules.programmer.blocks.ProgrammerTileEntity;
import mcjty.rftoolscontrol.modules.programmer.client.GuiProgrammer;
import mcjty.rftoolscontrol.setup.Registration;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import static mcjty.rftoolscontrol.setup.Registration.*;

public class ProgrammerModule implements IModule {

    public static final RegistryObject<BaseBlock> PROGRAMMER = BLOCKS.register("programmer", ProgrammerBlock::new);
    public static final RegistryObject<BlockEntityType<ProgrammerTileEntity>> PROGRAMMER_TILE = TILES.register("programmer", () -> BlockEntityType.Builder.of(ProgrammerTileEntity::new, PROGRAMMER.get()).build(null));
    public static final RegistryObject<Item> PROGRAMMER_ITEM = ITEMS.register("programmer", () -> new BlockItem(PROGRAMMER.get(), Registration.createStandardProperties()));
    public static final RegistryObject<MenuType<GenericContainer>> PROGRAMMER_CONTAINER = CONTAINERS.register("programmer", GenericContainer::createContainerType);

    @Override
    public void init(FMLCommonSetupEvent event) {

    }

    @Override
    public void initClient(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            GuiProgrammer.register();
        });
        ParameterEditors.init();
    }

    @Override
    public void initConfig() {

    }
}
