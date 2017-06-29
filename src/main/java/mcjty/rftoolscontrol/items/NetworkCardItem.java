package mcjty.rftoolscontrol.items;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class NetworkCardItem extends GenericRFToolsItem {

    private final int tier;

    public static final int TIER_NORMAL = 0;
    public static final int TIER_ADVANCED = 1;

    public NetworkCardItem(String name, int tier) {
        super(name);
        setMaxStackSize(1);
        this.tier = tier;
    }

    public int getTier() {
        return tier;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, World playerIn, List<String> list, ITooltipFlag advanced) {
        super.addInformation(stack, playerIn, list, advanced);
        list.add("Insert this in the processor to");
        list.add("allow access to nearby nodes");
        list.add("Use 'net setup <name>' in Processor");
        list.add("console to setup the network");
        if (tier == TIER_NORMAL) {
            list.add(TextFormatting.GREEN + "Range: 17x17x17 area");
        } else {
            list.add(TextFormatting.GREEN + "Range: 33x33x33 area");
            list.add(TextFormatting.GREEN + "Inter-process communication");
        }
    }
}
