package mcjty.rftoolscontrol.modules.multitank;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.modules.IModule;
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

import static mcjty.rftoolscontrol.setup.Registration.*;

public class MultiTankModule implements IModule {

    public static final RegistryObject<BaseBlock> MULTITANK = BLOCKS.register("tank", MultiTankBlock::new);
    public static final RegistryObject<BlockEntityType<MultiTankTileEntity>> MULTITANK_TILE = TILES.register("tank", () -> BlockEntityType.Builder.of(MultiTankTileEntity::new, MULTITANK.get()).build(null));
    public static final RegistryObject<Item> MULTITANK_ITEM = ITEMS.register("tank", () -> new BlockItem(MULTITANK.get(), Registration.createStandardProperties()));
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
}
