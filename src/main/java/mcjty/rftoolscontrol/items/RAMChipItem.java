package mcjty.rftoolscontrol.items;

import mcjty.rftoolscontrol.RFToolsControl;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;
import java.util.List;

public class RAMChipItem extends GenericRFToolsItem {

    public RAMChipItem() {
        super("ram_chip");
        setMaxStackSize(1);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, World playerIn, List<String> list, ITooltipFlag advanced) {
        super.addInformation(stack, playerIn, list, advanced);
        
        list.addAll(Arrays.asList(I18n.format("tooltips." + RFToolsControl.MODID + "." + "ram_chip").split("0x0a")));
    }

}
