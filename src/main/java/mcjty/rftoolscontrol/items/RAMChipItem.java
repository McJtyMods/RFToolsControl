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

public class RAMChipItem extends Item {

    public RAMChipItem() {
        super(new Properties()
                .maxStackSize(1)
                .group(RFToolsControl.setup.getTab()));

//        super((Properties) "ram_chip");
    }


    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> list, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, list, flagIn);
        list.add(new StringTextComponent("Insert this item in the processor"));
        list.add(new StringTextComponent("to get eight extra variables (max 32)"));
    }

}
