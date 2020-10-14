package mcjty.rftoolscontrol.items;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.resources.I18n;

import javax.annotation.Nullable;
import java.util.List;

public class CardBaseItem extends GenericRFToolsItem {

    public CardBaseItem() {
        super("card_base");
        setMaxStackSize(64);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World playerIn, List<String> list, ITooltipFlag advanced) {
        super.addInformation(stack, playerIn, list, advanced);
        list.addAll(Arrays.asList(I18n.format("tooltips." + RFToolsControl.MODID+"."+"card_base").split("0x0a")));

    }
}
