package mcjty.rftoolscontrol.compat;

import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

public class RFToolsStuff {

    public static final Supplier<Item> STORAGE_CONTROL_MODULE = () -> BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("rftoolsstorage", "storage_control_module"));
    public static final Supplier<Item> CRAFTING_CARD = () -> BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("rftoolsbase", "crafting_card"));

    public static void init() {
    }
}
