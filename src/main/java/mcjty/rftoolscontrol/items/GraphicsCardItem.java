package mcjty.rftoolscontrol.items;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.List;

public class GraphicsCardItem extends GenericRFToolsItem {

    public GraphicsCardItem() {
        super("graphics_card");
        setMaxStackSize(1);
    }


    @Override
    public void addInformation(ItemStack stack, World playerIn, List<String> list, ITooltipFlag advanced) {
        super.addInformation(stack, playerIn, list, advanced);
        list.add("The graphics card is needed to be able");
        list.add("to draw vector graphics");
    }
}
