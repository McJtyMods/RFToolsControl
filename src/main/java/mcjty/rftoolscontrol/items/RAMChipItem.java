package mcjty.rftoolscontrol.items;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.List;

public class RAMChipItem extends GenericRFToolsItem {

    public RAMChipItem() {
        super("ram_chip");
        setMaxStackSize(1);
    }


    @Override
    public void addInformation(ItemStack stack, World playerIn, List<String> list, ITooltipFlag advanced) {
        super.addInformation(stack, playerIn, list, advanced);
        list.add("Insert this item in the processor");
        list.add("to get eight extra variables (max 32)");
    }

}
