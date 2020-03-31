package mcjty.rftoolscontrol.modules.processor.items;

import mcjty.rftoolscontrol.RFToolsControl;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class GraphicsCardItem extends Item {

    public GraphicsCardItem() {
        super(new Properties()
                .maxStackSize(1)
                .group(RFToolsControl.setup.getTab()));
//        super((Properties) "graphics_card");
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> list, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, list, flagIn);
        list.add(new StringTextComponent("The graphics card is needed to be able"));
        list.add(new StringTextComponent("to draw vector graphics"));
    }
}
