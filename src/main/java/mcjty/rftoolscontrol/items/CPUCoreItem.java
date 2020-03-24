package mcjty.rftoolscontrol.items;

import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.config.ConfigSetup;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class CPUCoreItem extends Item {

    private final int tier;

    public CPUCoreItem(int tier) {
        super(new Properties()
                .maxStackSize(1)
                .group(RFToolsControl.setup.getTab()));
        this.tier = tier;
    }

    public int getTier() {
        return tier;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> list, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, list, flagIn);
        list.add(new StringTextComponent("This CPU core can be used in the"));
        list.add(new StringTextComponent("processor to allow it to run programs"));
        list.add(new StringTextComponent(TextFormatting.GREEN + "" + ConfigSetup.coreSpeed[tier].get() + " operations per tick"));
        list.add(new StringTextComponent(TextFormatting.GREEN + "" + ConfigSetup.coreRFPerTick[tier].get() + " RF per tick"));
    }
}
