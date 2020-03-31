package mcjty.rftoolscontrol.modules.programmer;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.rftoolscontrol.modules.programmer.blocks.ProgrammerBlock;
import mcjty.rftoolscontrol.modules.programmer.blocks.ProgrammerContainer;
import mcjty.rftoolscontrol.modules.programmer.blocks.ProgrammerTileEntity;
import mcjty.rftoolscontrol.setup.Registration;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;

import static mcjty.rftoolscontrol.setup.Registration.*;

public class ProgrammerSetup {

    public static void register() {
        // Needed to force class loading
    }

    public static final RegistryObject<BaseBlock> PROGRAMMER = BLOCKS.register("programmer", ProgrammerBlock::new);
    public static final RegistryObject<TileEntityType<ProgrammerTileEntity>> PROGRAMMER_TILE = TILES.register("programmer", () -> TileEntityType.Builder.create(ProgrammerTileEntity::new, PROGRAMMER.get()).build(null));
    public static final RegistryObject<Item> PROGRAMMER_ITEM = ITEMS.register("programmer", () -> new BlockItem(PROGRAMMER.get(), Registration.createStandardProperties()));
    public static final RegistryObject<ContainerType<ProgrammerContainer>> PROGRAMMER_CONTAINER = CONTAINERS.register("programmer", GenericContainer::createContainerType);

}
