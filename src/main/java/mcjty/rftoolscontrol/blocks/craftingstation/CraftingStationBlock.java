package mcjty.rftoolscontrol.blocks.craftingstation;

import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.blocks.GenericRFToolsBlock;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class CraftingStationBlock extends GenericRFToolsBlock<CraftingStationTileEntity, CraftingStationContainer> {

    public CraftingStationBlock() {
        super(Material.IRON, CraftingStationTileEntity.class, CraftingStationContainer::new, "craftingstation", false);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Class<GuiCraftingStation> getGuiClass() {
        return GuiCraftingStation.class;
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
