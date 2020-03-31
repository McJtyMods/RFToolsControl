package mcjty.rftoolscontrol.modules.craftingstation;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.rftoolscontrol.modules.craftingstation.blocks.CraftingStationBlock;
import mcjty.rftoolscontrol.modules.craftingstation.blocks.CraftingStationContainer;
import mcjty.rftoolscontrol.modules.craftingstation.blocks.CraftingStationTileEntity;
import mcjty.rftoolscontrol.setup.Registration;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;

import static mcjty.rftoolscontrol.setup.Registration.*;

public class CraftingStationSetup {

    public static void register() {
        // Needed to force class loading
    }

    public static final RegistryObject<BaseBlock> CRAFTING_STATION = BLOCKS.register("craftingstation", CraftingStationBlock::new);
    public static final RegistryObject<TileEntityType<CraftingStationTileEntity>> CRAFTING_STATION_TILE = TILES.register("craftingstation", () -> TileEntityType.Builder.create(CraftingStationTileEntity::new, CRAFTING_STATION.get()).build(null));
    public static final RegistryObject<Item> CRAFTING_STATION_ITEM = ITEMS.register("craftingstation", () -> new BlockItem(CRAFTING_STATION.get(), Registration.createStandardProperties()));
    public static final RegistryObject<ContainerType<CraftingStationContainer>> CRAFTING_STATION_CONTAINER = CONTAINERS.register("craftingstation", GenericContainer::createContainerType);

}
