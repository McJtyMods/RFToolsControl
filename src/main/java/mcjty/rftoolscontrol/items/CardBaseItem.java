package mcjty.rftoolscontrol.items;

import mcjty.rftoolscontrol.RFToolsControl;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class CardBaseItem extends Item {

    public CardBaseItem() {
        super(new Properties()
                .maxStackSize(64)
                .group(RFToolsControl.setup.getTab()));
//        super("card_base");
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> list, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, list, flagIn);
        list.add(new StringTextComponent("This item is the base ingredient"));
        list.add(new StringTextComponent("for many of the items and machines"));
        list.add(new StringTextComponent("in RFTools Control"));
    }
}
