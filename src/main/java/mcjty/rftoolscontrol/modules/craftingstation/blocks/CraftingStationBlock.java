package mcjty.rftoolscontrol.modules.craftingstation.blocks;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockBuilder;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;
import java.util.List;

public class CraftingStationBlock extends BaseBlock {

    public CraftingStationBlock() {
        super(new BlockBuilder()
                .tileEntitySupplier(CraftingStationTileEntity::new));
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable IBlockReader world, List<ITextComponent> tooltip, ITooltipFlag advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(new StringTextComponent("This block assists in auto crafting"));
        tooltip.add(new StringTextComponent("operations for a Processor"));
    }
}
