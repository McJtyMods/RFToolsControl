package mcjty.rftoolscontrol.modules.multitank;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.datagen.DataGen;
import mcjty.lib.datagen.Dob;
import mcjty.lib.modules.IModule;
import mcjty.rftoolsbase.modules.various.VariousModule;
import mcjty.rftoolscontrol.modules.multitank.blocks.MultiTankBlock;
import mcjty.rftoolscontrol.modules.multitank.blocks.MultiTankTileEntity;
import mcjty.rftoolscontrol.modules.multitank.client.GuiMultiTank;
import mcjty.rftoolscontrol.setup.Registration;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.RegistryObject;

import static mcjty.lib.datagen.DataGen.has;
import static mcjty.rftoolscontrol.RFToolsControl.tab;
import static mcjty.rftoolscontrol.setup.Registration.*;

public class MultiTankModule implements IModule {

    public static final RegistryObject<BaseBlock> MULTITANK = BLOCKS.register("tank", MultiTankBlock::new);
    public static final RegistryObject<BlockEntityType<MultiTankTileEntity>> TYPE_MULTITANK = TILES.register("tank", () -> BlockEntityType.Builder.of(MultiTankTileEntity::new, MULTITANK.get()).build(null));
    public static final RegistryObject<Item> MULTITANK_ITEM = ITEMS.register("tank", tab(() -> new BlockItem(MULTITANK.get(), Registration.createStandardProperties())));
    public static final RegistryObject<MenuType<GenericContainer>> MULTITANK_CONTAINER = CONTAINERS.register("tank", GenericContainer::createContainerType);

    @Override
    public void init(FMLCommonSetupEvent event) {

    }

    @Override
    public void initClient(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            GuiMultiTank.register();
        });
    }

    @Override
    public void initConfig() {

    }

    @Override
    public void initDatagen(DataGen dataGen) {
        dataGen.add(
                Dob.blockBuilder(MULTITANK)
                        .ironPickaxeTags()
                        .parentedItem("block/tank")
                        .standardLoot(TYPE_MULTITANK)
                        .shaped(builder -> builder
                                        .define('F', mcjty.rftoolsbase.modules.various.VariousModule.MACHINE_FRAME.get())
                                        .unlockedBy("frame", has(VariousModule.MACHINE_FRAME.get())),
                                "Fii", "iGG", "iGG")
        );
    }
}
