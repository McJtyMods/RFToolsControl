package mcjty.rftoolscontrol.compat;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public class RFToolsStuff {

    public static final Supplier<Item> STORAGE_CONTROL_MODULE = () -> BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("rftoolsstorage", "storage_control_module"));
    public static final Supplier<Item> CRAFTING_CARD = () -> BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("rftoolsbase", "crafting_card"));

    public static void init() {
    }
}
