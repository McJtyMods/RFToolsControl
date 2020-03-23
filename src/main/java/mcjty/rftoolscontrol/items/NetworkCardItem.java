package mcjty.rftoolscontrol.items;

import mcjty.rftoolscontrol.RFToolsControl;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class NetworkCardItem extends Item {

    private final int tier;

    public static final int TIER_NORMAL = 0;
    public static final int TIER_ADVANCED = 1;

    public NetworkCardItem(String name, int tier) {
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
        list.add(new StringTextComponent("Insert this in the processor to"));
        list.add(new StringTextComponent("allow access to nearby nodes"));
        list.add(new StringTextComponent("Use 'net setup <name>' in Processor"));
        list.add(new StringTextComponent("console to setup the network"));
        if (tier == TIER_NORMAL) {
            list.add(new StringTextComponent(TextFormatting.GREEN + "Range: 17x17x17 area"));
        } else {
            list.add(new StringTextComponent(TextFormatting.GREEN + "Range: 33x33x33 area"));
            list.add(new StringTextComponent(TextFormatting.GREEN + "Inter-process communication"));
        }
    }
}
