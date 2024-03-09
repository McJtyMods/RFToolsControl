package mcjty.rftoolscontrol.compat;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.ForgeRegistries;
import net.neoforged.neoforge.registries.RegistryObject;

public class RFToolsStuff {

    public static RegistryObject<Item> STORAGE_CONTROL_MODULE = RegistryObject.create(new ResourceLocation("rftoolsstorage", "storage_control_module"), BuiltInRegistries.ITEM);
    public static RegistryObject<Item> CRAFTING_CARD = RegistryObject.create(new ResourceLocation("rftoolsbase", "crafting_card"), BuiltInRegistries.ITEM);

    public static void init() {
    }
}
