package mcjty.rftoolscontrol.modules.craftingstation;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.modules.IModule;
import mcjty.rftoolscontrol.modules.craftingstation.blocks.CraftingStationBlock;
import mcjty.rftoolscontrol.modules.craftingstation.blocks.CraftingStationTileEntity;
import mcjty.rftoolscontrol.modules.craftingstation.client.GuiCraftingStation;
import mcjty.rftoolscontrol.setup.Registration;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import static mcjty.rftoolscontrol.setup.Registration.*;

public class CraftingStationModule implements IModule {

    public static final RegistryObject<BaseBlock> CRAFTING_STATION = BLOCKS.register("craftingstation", CraftingStationBlock::new);
    public static final RegistryObject<BlockEntityType<CraftingStationTileEntity>> CRAFTING_STATION_TILE = TILES.register("craftingstation", () -> BlockEntityType.Builder.of(CraftingStationTileEntity::new, CRAFTING_STATION.get()).build(null));
    public static final RegistryObject<Item> CRAFTING_STATION_ITEM = ITEMS.register("craftingstation", () -> new BlockItem(CRAFTING_STATION.get(), Registration.createStandardProperties()));
    public static final RegistryObject<MenuType<GenericContainer>> CRAFTING_STATION_CONTAINER = CONTAINERS.register("craftingstation", GenericContainer::createContainerType);

    @Override
    public void init(FMLCommonSetupEvent event) {

    }

    @Override
    public void initClient(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            GuiCraftingStation.register();
        });
    }

    @Override
    public void initConfig() {

    }
}
