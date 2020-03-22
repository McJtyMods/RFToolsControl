package mcjty.rftoolscontrol.blocks.craftingstation;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.rftoolscontrol.setup.GuiProxy;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.List;
import java.util.function.BiFunction;

public class CraftingStationBlock extends BaseBlock {

    public CraftingStationBlock() {
        super(Material.IRON, CraftingStationTileEntity.class, CraftingStationContainer::new, "craftingstation", false);
    }

    @Override
    public BiFunction<CraftingStationTileEntity, CraftingStationContainer, GenericGuiContainer<? super CraftingStationTileEntity>> getGuiFactory() {
        return GuiCraftingStation::new;
    }

    @Override
    public int getGuiID() {
        return GuiProxy.GUI_CRAFTINGSTATION;
    }


    @Override
    public void addInformation(ItemStack stack, World playerIn, List<String> list, ITooltipFlag advanced) {
        super.addInformation(stack, playerIn, list, advanced);
        list.add("This block assists in auto crafting");
        list.add("operations for a Processor");
    }
}
