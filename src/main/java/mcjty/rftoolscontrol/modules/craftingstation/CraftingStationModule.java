package mcjty.rftoolscontrol.modules.craftingstation;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.datagen.DataGen;
import mcjty.lib.datagen.Dob;
import mcjty.lib.modules.IModule;
import mcjty.rftoolscontrol.modules.craftingstation.blocks.CraftingStationBlock;
import mcjty.rftoolscontrol.modules.craftingstation.blocks.CraftingStationTileEntity;
import mcjty.rftoolscontrol.modules.craftingstation.client.GuiCraftingStation;
import mcjty.rftoolscontrol.modules.various.VariousModule;
import mcjty.rftoolscontrol.setup.Registration;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.RegistryObject;

import static mcjty.lib.datagen.DataGen.has;
import static mcjty.rftoolscontrol.setup.Registration.*;

public class CraftingStationModule implements IModule {

    public static final RegistryObject<BaseBlock> CRAFTING_STATION = BLOCKS.register("craftingstation", CraftingStationBlock::new);
    public static final RegistryObject<BlockEntityType<CraftingStationTileEntity>> TYPE_CRAFTING_STATION = TILES.register("craftingstation", () -> BlockEntityType.Builder.of(CraftingStationTileEntity::new, CRAFTING_STATION.get()).build(null));
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

    @Override
    public void initDatagen(DataGen dataGen) {
        dataGen.add(
                Dob.blockBuilder(CRAFTING_STATION)
                        .ironPickaxeTags()
                        .parentedItem("block/craftingstation")
                        .standardLoot(TYPE_CRAFTING_STATION)
                        .blockState(p -> p.orientedBlock(CRAFTING_STATION.get(), p.frontBasedModel("craftingstation", p.modLoc("block/machinecraftingstation"))))
                        .shaped(builder -> builder
                                        .define('F', mcjty.rftoolsbase.modules.various.VariousModule.MACHINE_FRAME.get())
                                        .define('M', VariousModule.CARD_BASE.get())
                                        .define('C', Items.CRAFTING_TABLE)
                                        .unlockedBy("cardbase", has(VariousModule.CARD_BASE.get())),
                                "rMr", "CFC", "rMr")
        );
    }
}
