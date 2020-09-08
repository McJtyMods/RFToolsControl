package mcjty.rftoolscontrol.modules.multitank;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.modules.IModule;
import mcjty.rftoolscontrol.modules.multitank.blocks.MultiTankBlock;
import mcjty.rftoolscontrol.modules.multitank.blocks.MultiTankContainer;
import mcjty.rftoolscontrol.modules.multitank.blocks.MultiTankTileEntity;
import mcjty.rftoolscontrol.modules.multitank.client.GuiMultiTank;
import mcjty.rftoolscontrol.setup.Registration;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import static mcjty.rftoolscontrol.setup.Registration.*;

public class MultiTankModule implements IModule {

    public static final RegistryObject<BaseBlock> MULTITANK = BLOCKS.register("tank", MultiTankBlock::new);
    public static final RegistryObject<TileEntityType<MultiTankTileEntity>> MULTITANK_TILE = TILES.register("tank", () -> TileEntityType.Builder.create(MultiTankTileEntity::new, MULTITANK.get()).build(null));
    public static final RegistryObject<Item> MULTITANK_ITEM = ITEMS.register("tank", () -> new BlockItem(MULTITANK.get(), Registration.createStandardProperties()));
    public static final RegistryObject<ContainerType<MultiTankContainer>> MULTITANK_CONTAINER = CONTAINERS.register("tank", GenericContainer::createContainerType);

    @Override
    public void init(FMLCommonSetupEvent event) {

    }

    @Override
    public void initClient(FMLClientSetupEvent event) {
        DeferredWorkQueue.runLater(() -> {
            GenericGuiContainer.register(MULTITANK_CONTAINER.get(), GuiMultiTank::new);
        });

        RenderTypeLookup.setRenderLayer(MULTITANK.get(), RenderType.getTranslucent());
    }

    @Override
    public void initConfig() {

    }
}
