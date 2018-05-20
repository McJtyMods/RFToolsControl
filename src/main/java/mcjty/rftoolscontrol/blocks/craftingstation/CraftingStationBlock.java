package mcjty.rftoolscontrol.blocks.craftingstation;

import mcjty.lib.gui.GenericGuiContainer;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.blocks.GenericRFToolsBlock;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.function.BiFunction;

public class CraftingStationBlock extends GenericRFToolsBlock<CraftingStationTileEntity, CraftingStationContainer> {

    public CraftingStationBlock() {
        super(Material.IRON, CraftingStationTileEntity.class, CraftingStationContainer::new, "craftingstation", false);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public BiFunction<CraftingStationTileEntity, CraftingStationContainer, GenericGuiContainer<? super CraftingStationTileEntity>> getGuiFactory() {
        return GuiCraftingStation::new;
    }

    @Override
    public int getGuiID() {
        return RFToolsControl.GUI_CRAFTINGSTATION;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, World playerIn, List<String> list, ITooltipFlag advanced) {
        super.addInformation(stack, playerIn, list, advanced);
        list.add("This block assists in auto crafting");
        list.add("operations for a Processor");
    }
}
